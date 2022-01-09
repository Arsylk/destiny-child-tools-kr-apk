package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.domain.*
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.common.OperationStateResult.*
import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckHeader
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.PckEncryption
import com.arsylk.mammonsmite.model.pck.PckEncryptionException
import com.arsylk.mammonsmite.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeHex
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@ExperimentalSerializationApi
class PckTools(override val json: Json): PckL2DTools, PckLocaleTools {
    override val pckTools = this

    fun packAsFlow(
        pck: UnpackedPckFile,
        file: File,
        log: LogLineChannel = EmptyLogLineChannel,
    ): Flow<OperationStateResult<Unit>> =
        flow {
            emit(Initial())
            RandomAccessFile(file, "rw").use { fs ->
                val count = pck.header.entries.size
                emit(InProgress(0, count + 1))
                log.info("File count: $count", "Pack")

                // calculate initial offset
                var offset = 8 + 4 + count * (8 + 1 + 4 + 4 + 8)

                // write pck header
                fs.write(PCK_IDENTIFIER)
                fs.pckWriteInt(count)
                pck.header.entries.forEach { entry ->
                    fs.pckWriteString(entry.hashString)
                    fs.pckWriteByte(0x00.toByte())
                    fs.pckWriteInt(offset)
                    val size = pck.getEntryFile(entry).length()
                    fs.pckWriteInt(size.toInt())
                    fs.pckWriteLong(size)

                    offset += size.toInt()
                }
                emit(InProgress(1, count + 1))

                // write pck entry files
                pck.header.entries.forEachIndexed { i, entry ->
                    val entryFile = pck.getEntryFile(entry)
                    fs.write(entryFile.readBytes())
                    emit(InProgress(i + 1, count + 1))
                    val logLine = String.format(
                        "File %2d/%d %s",
                        i + 1,
                        count,
                        entry.hashString,
                    )
                    log.info(logLine, "Pack")
                }

                log.info("Packed successfully:", "Pack")
                log.info(file.absolutePath, "Pack")
                emit(Success(Unit))
            }
        }
        .onStart { file.runCatching(File::delete) }
        .catch { t ->
            emit(Failure(t))
            log.error(t, "Pack")
        }
        .flowOn(Dispatchers.IO)

    fun readPackedPck(
        file: File,
        log: LogLineChannel = EmptyLogLineChannel,
    ): Flow<OperationStateResult<PackedPckFile>> =
        flow {
            val entries = mutableListOf<PackedPckEntry>()

            //buffer src bytes
            emit(Initial())
            RandomAccessFile(file, "r").use { fs ->
                val mbb = fs.channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, fs.length())
                    .load()
                mbb.order(ByteOrder.LITTLE_ENDIAN)
                mbb.position(0)

                //begin byte analysis
                val identifier = ByteArray(8)
                //byte(8) pck identifier
                mbb.get(identifier)
                if (identifier.contentEquals(PCK_IDENTIFIER)) {
                    //byte(4) count
                    val count = mbb.int
                    log.info("File count: $count", "Read")
                    for (i in 0 until count) {
                        //byte(8) hash
                        val hash = ByteArray(8)
                        mbb.get(hash)
                        //byte(1) flag
                        val flag = mbb.get().toInt()
                        //byte(4) offset
                        val offset = mbb.int
                        //byte(4) compressed size
                        val sizeCompressed = mbb.int
                        //byte(4) original size
                        val sizeOriginal = mbb.int
                        //byte(4) ???
                        mbb.position(mbb.position() + 4)

                        emit(InProgress(i + 1, count))
                        entries += PackedPckEntry(
                            index = i,
                            hash = hash,
                            flag = flag,
                            offset = offset,
                            sizeOriginal = sizeOriginal,
                            sizeCompressed = sizeCompressed,
                        )
                    }
                } else {
                    mbb.runCatching(MappedByteBuffer::clear)
                    throw IllegalArgumentException("Not a pck file")
                }

                mbb.runCatching(MappedByteBuffer::clear)
            }

            val packedPckFile = PackedPckFile(
                file = file,
                header = PackedPckHeader(
                    entries = entries,
                )
            )
            emit(Success(packedPckFile))
        }
        .catch { t ->
            emit(Failure(t))
            log.error(t, "Read")
        }
        .flowOn(Dispatchers.IO)

    fun unpackAsFlow(
        packedPckFile: PackedPckFile,
        folder: File,
        log: LogLineChannel = EmptyLogLineChannel,
    ) = flow { emit(Unit) }
        .transform {
            for (key in PckEncryption.values()) {
                try {
                    unpackAsFlow(packedPckFile, folder, key, log)
                        .collect { state ->
                            emit(state)
                            if (state is Failure)
                                throw state.t
                        }
                    break
                }catch(t: Throwable) {
                    if (t !is PckEncryptionException)
                        throw t
                }
            }
        }
        .catch { t ->
            emit(Failure(t))
            log.error(t, "Unpack")
        }

    fun unpackAsFlow(
        packedPckFile: PackedPckFile,
        folder: File,
        key: PckEncryption,
        log: LogLineChannel = EmptyLogLineChannel,
    ) =
        flow {
            val entries = mutableListOf<UnpackedPckEntry>()

            //buffer src bytes
            emit(Initial())
            RandomAccessFile(packedPckFile.file, "r").use { fs ->
                val mbb = fs.channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, fs.length())
                    .load()
                mbb.order(ByteOrder.LITTLE_ENDIAN)
                mbb.position(0)


                //begin byte analysis
                val identifier = ByteArray(8)
                //byte(8) pck identifier
                mbb.get(identifier)
                if (identifier.contentEquals(PCK_IDENTIFIER)) {
                    packedPckFile.header.entries.forEachIndexed { i, packedEntry ->
                        //start extract file
                        mbb.position(packedEntry.offset)
                        val rawFileBytes = ByteArray(packedEntry.sizeCompressed)
                        mbb.get(rawFileBytes)

                        // try-catch encryption key
                        val fileBytes = try {
                            val decryptedBytes = if (packedEntry.flag and 2 != 0)
                                Utils.aes_decrypt(rawFileBytes, key.key)
                            else rawFileBytes

                            // decompress with yappy
                            if (packedEntry.flag and 1 != 0)
                                Utils.yappy_uncompress(decryptedBytes, packedEntry.sizeOriginal)
                            else decryptedBytes
                        }catch (t: Throwable) {
                            throw PckEncryptionException(key, t)
                        }


                        val type = PckEntryFileType
                            .fromByte(fileBytes.getOrNull(0))


                        //log progress
                        val logLine = String.format(
                            "File %2d/%d %s [%016X | %8d] %02d %s",
                            i + 1,
                            packedPckFile.header.entries.size,
                            packedEntry.hashString,
                            packedEntry.offset,
                            packedEntry.sizeOriginal,
                            packedEntry.flag,
                            type
                        )
                        log.info(logLine, "Unpack")


                        //save extracted file (files starting with _ are unprocessed)
                        val filename = String.format("%08d.%s", packedEntry.index, type.extension)
                        val outputFile = File(folder, filename)
                        FileOutputStream(outputFile).buffered().use { it.write(fileBytes) }

                        emit(InProgress(i + 1, packedPckFile.header.entries.size))
                        entries += UnpackedPckEntry(
                            filename = filename,
                            index = packedEntry.index,
                            hashString = packedEntry.hashString,
                        )
                    }
                } else {
                    mbb.runCatching(MappedByteBuffer::clear)
                    throw IllegalArgumentException("Not a pck file")
                }
                mbb.runCatching(MappedByteBuffer::clear)
            }

            val pck = UnpackedPckFile(
                folder = folder,
                header = UnpackedPckHeader(
                    entries = entries,
                ),
            )
            writeUnpackedPckHeader(pck)
            emit(Success(pck))
        }
        .onStart {
            kotlin.runCatching { folder.mkdirs() }
            kotlin.runCatching { folder.safeListFiles().forEach(File::delete) }
        }
        .catch { t ->
            emit(Failure(t))
            log.error(t, "Unpack")
        }
        .flowOn(Dispatchers.IO)

    suspend fun updateEntry(
        unpackedPck: UnpackedPckFile,
        newEntry: UnpackedPckEntry,
        onUpdate: suspend (oldEntry: UnpackedPckEntry, newEntry: UnpackedPckEntry) -> Unit = { _, _ -> }
    ): UnpackedPckFile {
        return with(unpackedPck) {
            var updated = false
            val newPckFile = UnpackedPckFile(
                folder = folder,
                header = header.copy(
                    entries = header.entries.map { entry ->
                        if (entry.hashString == newEntry.hashString) {
                            val oldFile = getEntryFile(entry)
                            val newFile = getEntryFile(newEntry)
                            if (oldFile != newFile) {
                                if (newFile.exists())
                                    throw IllegalStateException("Trying to rename to existing file")
                                oldFile.renameTo(newFile)
                                updated = true
                                onUpdate.invoke(entry, newEntry)
                            }
                            newEntry
                        } else entry
                    }
                )
            )


            if (updated) writeUnpackedPckHeader(newPckFile)
            newPckFile
        }
    }


    @Throws(IOException::class)
    suspend fun writeUnpackedPckHeader(pck: UnpackedPckFile) {
        withContext(Dispatchers.IO) {
            json.encodeToFile(pck.header, pck.headerFile)
        }
    }

    @Throws(IOException::class)
    suspend fun saveUnpackedPck(pck: UnpackedPckFile, folder: File): UnpackedPckFile {
        return withContext(Dispatchers.IO) {
            pck.folder.safeListFiles().forEach {
                it.copyTo(File(folder, it.name))
            }
            pck.copy(folder = folder)
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun readUnpackedPck(folder: File): UnpackedPckFile {
        return withContext(Dispatchers.IO) {
            val file = File(folder, UnpackedPckFile.HEADER_FILENAME)
            val header = json.decodeFromFile<UnpackedPckHeader>(file)
            UnpackedPckFile(folder, header)
        }
    }

    @Throws(IOException::class)
    suspend fun deleteUnpackedPck(pck: UnpackedPckFile) {
        return withContext(Dispatchers.IO) {
            pck.folder.safeListFiles().onEach(File::delete)
            pck.folder.delete()
        }
    }

    companion object {
        val PCK_IDENTIFIER = byteArrayOf(
            0x50.toByte(),
            0x43.toByte(),
            0x4B.toByte(),
            0x00.toByte(),
            0xCD.toByte(),
            0xCC.toByte(),
            0xCC.toByte(),
            0x3E.toByte()
        )
    }
}

internal fun RandomAccessFile.pckWriteByte(byte: Byte) =
    write(ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(byte).array())

internal fun RandomAccessFile.pckWriteInt(int: Int) =
    write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(int).array())

internal fun RandomAccessFile.pckWriteLong(long: Long) =
    write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(long).array())

internal fun RandomAccessFile.pckWriteString(string: String) =
    write(string.decodeHex().toByteArray())