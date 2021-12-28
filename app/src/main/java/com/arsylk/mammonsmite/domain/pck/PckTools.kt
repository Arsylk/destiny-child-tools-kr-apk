package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.domain.*
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.common.OperationStateResult.*
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DExpression
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DHeader
import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckHeader
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.live2d.L2DModelInfo
import com.arsylk.mammonsmite.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.*
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@ExperimentalSerializationApi
class PckTools(
    private val json: Json
) {

    fun readPackedPckAsFlow(
        file: IFile,
        log: SendChannel<LogLine> = LogLineChannel.Default
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
        log: SendChannel<LogLine> = LogLineChannel.Default
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

                        // try-catch both encryption keys
                        val decryptedBytes = if (packedEntry.flag and 2 != 0) try {
                            Utils.aes_decrypt(rawFileBytes, 0)
                        } catch (_: Throwable) {
                            Utils.aes_decrypt(rawFileBytes, 1)
                        } else rawFileBytes

                        // decompress with yappy
                        val fileBytes = if (packedEntry.flag and 1 != 0)
                            Utils.yappy_uncompress(decryptedBytes, packedEntry.sizeOriginal)
                        else decryptedBytes

                        val type = PckEntryFileType
                            .fromByte(fileBytes.getOrNull(0))


                        //log progress
                        val logLine = String.format(
                            "File %2d/%d %s [%016X | %6d] %02d %s",
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
            writeUnpackedPckFileHeader(pck)
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

    fun unpackedPckFileToModel(
        unpackedPck: UnpackedPckFile,
        log: SendChannel<LogLine> = LogLineChannel.Default
    ) =
        flow {
            emit(Initial())
            val (modelInfoEntry, modelInfo) = findModelJson(unpackedPck)
                ?: throw IllegalArgumentException("Could not find or parse model.json")
            emit(InProgress(10.0f))

            val entryLogger: suspend (UnpackedPckEntry, UnpackedPckEntry) -> Unit = { old, new ->
                log.info("Renamed '${old.filename}' to '${new.filename}'", tag = "Model")
            }

            var pck = unpackedPck.updateEntry(modelInfoEntry, entryLogger)
            val textureEntries = findModelTextures(pck, modelInfo)
            textureEntries.forEach { pck = pck.updateEntry(it, entryLogger) }
            emit(InProgress(20.0f))


            val characterDatEntry = findCharacterDat(pck, modelInfo)
            pck = pck.updateEntry(characterDatEntry, entryLogger)
            emit(InProgress(30.0f))


            val motionEntries = findMotions(pck, modelInfo)
            val viewIdx = motionEntries.firstNotNullOfOrNull { ViewIdx.parse(it.filename) }
            log.info("View Idx: ${viewIdx?.string}", tag = "Model")

            motionEntries.forEach { pck = pck.updateEntry(it, entryLogger) }
            emit(InProgress(60.0f))


            val expressionEntries = findExpressions(pck, modelInfoEntry, modelInfo)
            expressionEntries.forEach { pck = pck.updateEntry(it, entryLogger) }
            emit(InProgress(90.0f))

            val l2dFile = L2DFile(
                folder = pck.folder,
                header = L2DHeader(
                    modelInfoFilename = modelInfoEntry.filename,
                    viewIdx = viewIdx,
                )
            )

            writeL2DFileHeader(l2dFile)
            emit(InProgress(100.0f))

            emit(Success(pck to l2dFile))
        }
        .catch { t ->
            emit(Failure(t))
            log.error(t, "Model")
        }
        .flowOn(Dispatchers.IO)


    private suspend fun findModelJson(pck: UnpackedPckFile): Pair<UnpackedPckEntry, L2DModelInfo>? {
        return withContext(Dispatchers.IO) {
            pck.getEntries(PckEntryFileType.JSON, HASH_MODEL_OR_TEXTURE)
                .firstNotNullOfOrNull { entry ->
                    kotlin.runCatching {
                        val modelInfo = json.decodeFromFile<L2DModelInfo>(pck.getEntryFile(entry))
                        val newEntry = entry.copy(filename = MODEL_INFO_NAME)

                        newEntry to modelInfo
                    }.getOrNull()
                }
        }
    }

    private suspend fun findModelTextures(
        pck: UnpackedPckFile,
        modelInfo: L2DModelInfo
    ): List<UnpackedPckEntry> {
        return withContext(Dispatchers.IO) {
            var list = pck.getEntries(PckEntryFileType.PNG, HASH_MODEL_OR_TEXTURE)
            if (modelInfo.textures.size > list.size)
                list = pck.getEntries(PckEntryFileType.PNG)
            modelInfo.textures.mapIndexedNotNull { i, texture ->
                list.getOrNull(i)?.copy(filename = texture)
            }
        }
    }

    @Throws(IllegalStateException::class)
    private suspend fun findCharacterDat(
        pck: UnpackedPckFile,
        modelInfo: L2DModelInfo
    ): UnpackedPckEntry {
        return withContext(Dispatchers.IO) {
            val entry = pck
                .getEntries(PckEntryFileType.DAT, HASH_CHARACTER_DAT)
                .firstOrNull()
                ?: pck.getEntries(PckEntryFileType.DAT).firstOrNull()
                ?: throw IllegalStateException("Character dat file not found")
            entry.copy(filename = modelInfo.model)
        }
    }

    private suspend fun findMotions(
        pck: UnpackedPckFile,
        modelInfo: L2DModelInfo
    ): List<UnpackedPckEntry> {
        return withContext(Dispatchers.IO) {
            val list = modelInfo.getSortedMotions()
            pck.getEntries(PckEntryFileType.MTN)
                .mapIndexedNotNull { i, entry ->
                    list.getOrNull(i)?.let { (_, v) ->
                        entry.copy(filename = v.filename)
                    }
                }
        }
    }

    private suspend fun findExpressions(
        pck: UnpackedPckFile,
        modelInfoEntry: UnpackedPckEntry,
        modelInfo: L2DModelInfo
    ): List<UnpackedPckEntry> {
        return withContext(Dispatchers.IO) {
            pck.getEntries(PckEntryFileType.JSON)
                .filter { it.hashString != modelInfoEntry.hashString }
                .mapIndexedNotNull { i, entry ->
                    kotlin.runCatching {
                        val expression = json.decodeFromFile<L2DExpression>(pck.getEntryFile(entry))
                        entry.copy(filename = modelInfo.expressions[i].filename)
                    }.getOrElse {
                        it.printStackTrace()
                        return@mapIndexedNotNull null
                    }
                }
        }
    }

    private suspend fun UnpackedPckFile.updateEntry(
        newEntry: UnpackedPckEntry,
        onUpdate: suspend (oldEntry: UnpackedPckEntry, newEntry: UnpackedPckEntry) -> Unit = { _, _ -> }
    ): UnpackedPckFile {
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

        if (updated) writeUnpackedPckFileHeader(newPckFile)
        return newPckFile
    }


    @Throws(IOException::class)
    suspend fun writeUnpackedPckFileHeader(pck: UnpackedPckFile) {
        withContext(Dispatchers.IO) {
            json.encodeToFile(pck.header, pck.headerFile)
        }
    }

    @Throws(IOException::class)
    suspend fun writeL2DFileHeader(l2dFile: L2DFile) {
        withContext(Dispatchers.IO) {
            json.encodeToFile(l2dFile.header, l2dFile.modelFile)
        }
    }

    @Throws(IOException::class)
    suspend fun saveUnpackedPckFile(pck: UnpackedPckFile, folder: File): UnpackedPckFile {
        return withContext(Dispatchers.IO) {
            pck.folder.safeListFiles().forEach {
                it.copyTo(File(folder, it.name))
            }
            pck.copy(folder = folder)
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun readUnpackedPckFile(folder: File): UnpackedPckFile {
        return withContext(Dispatchers.IO) {
            val file = File(folder, UnpackedPckFile.HEADER_FILENAME)
            val header = json.decodeFromFile<UnpackedPckHeader>(file)
            UnpackedPckFile(folder, header)
        }
    }

    companion object {
        private val PCK_IDENTIFIER = byteArrayOf(
            0x50.toByte(),
            0x43.toByte(),
            0x4B.toByte(),
            0x00.toByte(),
            0xCD.toByte(),
            0xCC.toByte(),
            0xCC.toByte(),
            0x3E.toByte()
        )
        private val HASH_MODEL_OR_TEXTURE = "660E0026"
        private val HASH_CHARACTER_DAT = "050E0025"

        const val MODEL_INFO_NAME = "model.json"
    }
}