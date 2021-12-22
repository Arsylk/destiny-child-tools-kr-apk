package com.arsylk.mammonsmite.model.api.response

import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import com.arsylk.mammonsmite.model.destinychild.LocalePatchFile
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import org.json.JSONException

data class LocalePatchResponse(
    @SerializedName("name")
    val name: String,

    @SerializedName("date")
    val date: String?,

    @SerializedName("files")
    val files: Map<String, JsonObject>
) {

    fun parse(): LocalePatch {
        return LocalePatch(
            name = name,
            files = buildMap {
                files.forEach { (k, json) ->
                    kotlin.runCatching {
                        val hash = json.get("hash").asString ?: return@forEach
                        val lineType = when (json.get("line_type").asInt) {
                            0 -> LocalePatchFile.Type.TABLE
                            1 -> LocalePatchFile.Type.DICT
                            else -> return@forEach
                        }
                        this[k] = LocalePatchFile(
                            hash = hash,
                            type = lineType,
                            dict = json.getAsJsonObject("dict")
                                .entrySet()
                                .mapNotNull { entry ->
                                    (entry.key ?: return@mapNotNull null) to (entry.value.asString ?: return@mapNotNull null)
                                }
                                .associate { it },
                        )
                    }
                }
            }
        )
    }
}