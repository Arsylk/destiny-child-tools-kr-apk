package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.domain.decodeFromFile
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.common.*
import com.arsylk.mammonsmite.model.common.OperationStateResult.*
import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import com.arsylk.mammonsmite.model.destinychild.LocalePatchFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

@ExperimentalSerializationApi
interface PckLocaleTools {
    val json: Json
    val pckTools: PckTools

    fun unpackedPckToLocalePatch(
        pck: UnpackedPckFile,
        log: SendChannel<LogLine> = LogLineChannel.Default
    ) = flow {
        emit(Initial())

        val entries = pck.header.entries
        val patchFiles = entries.mapIndexedNotNull { i, entry ->
            emit(InProgress(i * 2, entries.size * 2))

            val result = kotlin.runCatching { parseLocalePatchFile(pck, entry) }
            result.onSuccess { log.info("Parsed ${it.dict.size} lines as ${it.type}", "Locale") }
            result.onFailure { log.warn(it, "Locale") }

            emit(InProgress(i * 2 + 1, entries.size * 2))
            result.getOrNull()?.let { entry.hashString to it }
        }

        val patch = LocalePatch(
            name = pck.header.name,
            date = "now ?",
            files = patchFiles.toMap(),
        )
        emit(Success(patch))
    }
    .catch { t ->
        emit(Failure(t))
        log.error(t, "Locale")
    }
    .flowOn(Dispatchers.IO)


    @Throws(IOException::class)
    suspend fun parseLocalePatchFile(pck: UnpackedPckFile, entry: UnpackedPckEntry): LocalePatchFile {
        return withContext(Dispatchers.IO) {
            val map = mutableMapOf<String, String>()

            val lines = pck.getEntryFile(entry).readLines()
            var regex: Regex? = null
            for (line in lines) {
                // ignore blank & comment lines
                if (line.isBlank() || line.getOrNull(0) == '/' || line.getOrNull(1) == '/')
                    continue

                // find pattern
                regex = when {
                    regex != null -> regex
                    LOCALE_DEF_REGEX.matches(line) -> LOCALE_DEF_REGEX
                    LOCALE_TAB_REGEX.matches(line) -> LOCALE_TAB_REGEX
                    else -> null
                }

                // apply matcher to line
                if (regex != null) {
                    val result = regex.find(line)
                    when {
                        result != null -> use(result.groupValues[1], result.groupValues[2], map::put)
                        regex == LOCALE_DEF_REGEX -> {
                            // legacy code that did something ?
                            if (line.contains("=")) {
                                kotlin.runCatching {
                                    val index = line.indexOf("=")
                                    val key = line.substring(0, index).trim()
                                    val value = line.substring(index + 1, 0)
                                        .trim().trim('"')
                                    map[key] = value
                                }
                            }
                        }
                    }
                }
            }
            val type = when (regex) {
                LOCALE_DEF_REGEX -> LocalePatchFile.Type.DICT
                LOCALE_TAB_REGEX -> LocalePatchFile.Type.TABLE
                else -> LocalePatchFile.Type.UNKNOWN
            }

            LocalePatchFile(
                hash = entry.hashString,
                type = type,
                dict = map.toMap(),
            )
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun jsonFileToLocalePatch(file: File): LocalePatch {
        return withContext(Dispatchers.IO) {
            json.decodeFromFile(file)
        }
    }


    companion object {
        val LOCALE_DEF_REGEX = "^(?!/)(\\S+)\\s*?=\\s*?\"(.*?)\"\\s*?$".toRegex()
        val LOCALE_TAB_REGEX = "^(?!/)(\\S+)\\s(.*?)$".toRegex()
    }
}