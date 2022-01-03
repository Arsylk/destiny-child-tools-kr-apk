package com.arsylk.mammonsmite.domain.pck

import com.arsylk.mammonsmite.DestinyChild.DCDefine
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckEntry
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.regex.Pattern

@ExperimentalSerializationApi
interface PckLocaleTools {
    val json: Json
    val pckTools: PckTools

    @Throws(IOException::class)
    suspend fun unpackedPckToLocale(pck: UnpackedPckFile) {
        pck.header.entries.forEach { entry ->
            println("${entry.filename} ${entry.hashString} ${entry.type}")
            findLocaleFiles(pck, entry)
        }
    }

    @Throws(IOException::class, SerializationException::class)
    suspend fun findLocaleFiles(pck: UnpackedPckFile, entry: UnpackedPckEntry): List<UnpackedPckEntry> {
        return withContext(Dispatchers.IO) {
            val map = mutableMapOf<String, String>()

            val lines = pck.getEntryFile(entry).readLines()
            var pattern: Pattern? = null
            for (line in lines) {
                if (line.getOrNull(0) == '/' || line.getOrNull(1) == '/')
                    continue
                if (pattern == null) {
                    pattern = when {
                        line.contains("=") && LOCALE_DEF_LINE_PATTERN.matcher(line).matches() ->
                            LOCALE_DEF_LINE_PATTERN
                        LOCALE_TAB_LINE_PATTERN.matcher(line).matches() -> LOCALE_TAB_LINE_PATTERN
                        else -> null
                    }
                }
                if (pattern != null) {
                    val matcher = pattern.matcher(line)
                    when {
                        matcher.matches() ->
                            map[matcher.group(1)!!] = matcher.group(2)!!
                        pattern == LOCALE_DEF_LINE_PATTERN -> {
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

            println("found: ${map.size} lines")
            emptyList()
        }
    }

    companion object {
        val LOCALE_DEF_LINE_PATTERN = Pattern.compile("^(?!/)(\\S+)\\s*?=\\s*?\"(.*?)\"\\s*?$")
        val LOCALE_TAB_LINE_PATTERN = Pattern.compile("^(?!/)(\\S+)\\s(.*?)$")
    }
}