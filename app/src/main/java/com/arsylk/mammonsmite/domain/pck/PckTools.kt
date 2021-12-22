package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.domain.asSuccess
import com.arsylk.mammonsmite.domain.fromJson
import com.arsylk.mammonsmite.domain.safeListFiles
import com.arsylk.mammonsmite.model.common.OperationStateResult
import com.arsylk.mammonsmite.model.common.OperationStateResult.*
import com.arsylk.mammonsmite.model.live2d.L2DExpression
import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckFile
import com.arsylk.mammonsmite.model.pck.packed.PackedPckHeader
import com.arsylk.mammonsmite.model.pck.packed.PackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckHeader
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.model.ModelInfoExpression
import com.arsylk.mammonsmite.model.pck.unpacked.model.ModelPckModelInfo
import com.arsylk.mammonsmite.utils.Utils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class PckTools(private val gson: Gson) {

    fun readPackedPckAsFlow(file: File): Flow<OperationStateResult<PackedPckFile>> =
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
        .catch { t -> emit(Failure(t)) }
        .flowOn(Dispatchers.IO)

    @Throws(Throwable::class)
    suspend fun readPackedPck(file: File): PackedPckFile {
        return readPackedPckAsFlow(file).asSuccess()
    }

    fun unpackAsFlow(packedPckFile: PackedPckFile, folder: File) =
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
        .catch { t -> emit(Failure(t)) }
        .flowOn(Dispatchers.IO)

    @Throws(IOException::class)
    suspend fun writeUnpackedPckFileHeader(unpackedPck: UnpackedPckFile) {
        withContext(Dispatchers.IO) {
            val file = unpackedPck.headerFile
            if (file.exists()) file.delete()
            FileWriter(unpackedPck.headerFile).buffered().use {
                it.write(gson.toJson(unpackedPck.header))
            }
        }
    }

    fun unpackedPckFileToModel(unpackedPck: UnpackedPckFile) =
        flow {
            val (newEntry, modelInfo) = findModelJson(unpackedPck)
                ?: throw IllegalArgumentException("Could not find or parse model_info.json")

            var pck = unpackedPck.updateEntry(newEntry)
            val textureEntries = findModelTextures(pck, modelInfo)
            textureEntries.forEach { pck = pck.updateEntry(it) }

            val characterDatEntry = findCharacterDat(pck, modelInfo)
            pck = pck.updateEntry(characterDatEntry)

            val motionEntries = findMotions(pck, modelInfo)
            motionEntries.forEach { pck = pck.updateEntry(it) }

            val expressionEntries = findExpressions(pck, modelInfo)
            expressionEntries.forEach { pck = pck.updateEntry(it) }

            emit(pck)
        }


    private suspend fun findModelJson(pck: UnpackedPckFile): Pair<UnpackedPckEntry, ModelPckModelInfo>? {
        return withContext(Dispatchers.IO) {
            pck.getEntries(PckEntryFileType.JSON, HASH_MODEL_OR_TEXTURE)
                .firstNotNullOfOrNull { entry ->
                    kotlin.runCatching {
                        val modelInfo = gson.fromJson(
                            pck.getEntryFile(entry),
                            ModelPckModelInfo::class.java,
                        )
                        assert(modelInfo.model != null)
                        assert(modelInfo.textures != null)
                        assert(modelInfo.motionMap != null)
                        val newEntry = entry.copy(filename = MODEL_INFO_NAME)

                        newEntry to modelInfo
                    }.getOrNull()
                }
        }
    }

    private suspend fun findModelTextures(pck: UnpackedPckFile, modelInfo: ModelPckModelInfo): List<UnpackedPckEntry> {
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
    private suspend fun findCharacterDat(pck: UnpackedPckFile, modelInfo: ModelPckModelInfo): UnpackedPckEntry {
        return withContext(Dispatchers.IO) {
            val entry = pck
                .getEntries(PckEntryFileType.DAT, HASH_CHARACTER_DAT)
                .firstOrNull()
                ?: pck.getEntries(PckEntryFileType.DAT).firstOrNull()
                ?: throw IllegalStateException("Character dat file not found")
            entry.copy(filename = modelInfo.model)
        }
    }

    private suspend fun findMotions(pck: UnpackedPckFile, modelInfo: ModelPckModelInfo): List<UnpackedPckEntry> {
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

    private suspend fun findExpressions(pck: UnpackedPckFile, modelInfo: ModelPckModelInfo): List<UnpackedPckEntry> {
        return withContext(Dispatchers.IO) {
            pck.getEntries(PckEntryFileType.JSON)
                .mapIndexedNotNull { i, entry ->
                    kotlin.runCatching {
                        val expression = gson.fromJson(
                            pck.getEntryFile(entry),
                            L2DExpression::class.java,
                        )
                        assert(expression.type == L2DExpression.TYPE)
                        entry.copy(filename = modelInfo.expressions[i].filename)
                    }.getOrNull()
                }
        }
    }

    private suspend fun UnpackedPckFile.updateEntry(newEntry: UnpackedPckEntry): UnpackedPckFile {
        var updated = false
        val newPckFile = UnpackedPckFile(
            folder = folder,
            header = UnpackedPckHeader(
                entries = header.entries.map { entry ->
                    if (entry.hashString == newEntry.hashString) {
                        val oldFile = getEntryFile(entry)
                        val newFile = getEntryFile(newEntry)
                        if (oldFile != newFile) {
                            if (newFile.exists())
                                throw IllegalStateException("Trying to rename to existing file")
                            oldFile.renameTo(newFile)
                            updated = true
                        }
                        newEntry
                    }
                    else entry
                }
            )
        )

        if (updated) writeUnpackedPckFileHeader(newPckFile)
        return newPckFile
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

        const val MODEL_INFO_NAME = "model_info.json"
    }
}