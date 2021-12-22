package com.arsylk.mammonsmite.model.api.response

import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

typealias CharacterSkinDataFileResponse = Map<String, JsonElement>

fun CharacterSkinDataFileResponse.parse(): List<CharacterSkinData> {
    return mapNotNull { (key, item) ->
        kotlin.runCatching {
            val viewIdxList = mutableListOf<ViewIdx>()
            fun innerParse(json: JsonElement?) {
                when {
                    json is JsonObject && json.has("view_idx") -> {
                        kotlin.runCatching {
                            val viewIdxString = json.get("view_idx").asString
                            ViewIdx.parse(viewIdxString)?.also(viewIdxList::add)
                        }
                    }
                    json is JsonObject -> json.entrySet()
                        .forEach { (_, v) -> innerParse(v) }
                    json is JsonArray -> json
                        .forEach(::innerParse)

                }
            }
            innerParse(item)
            CharacterSkinData(key, viewIdxList)
        }.getOrNull()
    }
}

data class CharacterSkinData(val idx: String, val viewIdxList: List<ViewIdx>)