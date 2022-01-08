package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.domain.decodeFromFile
import com.arsylk.mammonsmite.domain.encodeToFile
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.common.OperationStateResult.*
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.arsylk.mammonsmite.model.live2d.L2DExpression
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.live2d.L2DHeader
import com.arsylk.mammonsmite.model.live2d.L2DModelInfo
import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.IOException

@ExperimentalSerializationApi
interface PckL2DTools {
    val json: Json
    val pckTools: PckTools

    fun unpackedPckToModel(
        unpackedPck: UnpackedPckFile,
        log: SendChannel<LogLine> = LogLineChannel.Default
    ) = flow {
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

            pckTools.writeL2DFileHeader(l2dFile)
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

    @Throws(IOException::class)
    suspend fun writeL2DFileHeader(l2dFile: L2DFile) {
        withContext(Dispatchers.IO) {
            json.encodeToFile(l2dFile.header, l2dFile.modelFile)
        }
    }

    private suspend fun UnpackedPckFile.updateEntry(
        newEntry: UnpackedPckEntry,
        onUpdate: suspend (oldEntry: UnpackedPckEntry, newEntry: UnpackedPckEntry) -> Unit = { _, _ -> }
    ): UnpackedPckFile = pckTools.updateEntry(this, newEntry, onUpdate)

    companion object {
        const val HASH_MODEL_OR_TEXTURE = "660e0026"
        const val HASH_CHARACTER_DAT = "050e0025"

        const val MODEL_INFO_NAME = "model.json"
    }
}