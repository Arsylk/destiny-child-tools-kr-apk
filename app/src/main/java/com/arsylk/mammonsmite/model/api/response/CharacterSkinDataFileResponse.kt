package com.arsylk.mammonsmite.model.api.response

import com.arsylk.mammonsmite.model.destinychild.CharacterSkinData
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

typealias CharacterSkinDataFileResponse = JsonObject

fun CharacterSkinDataFileResponse.parse(): List<CharacterSkinData> {
    return mapNotNull { (key, item) ->
        kotlin.runCatching {
            val viewIdxList = mutableListOf<ViewIdx>()
            fun innerParse(json: JsonElement?) {
                when {
                    json is JsonObject && json.containsKey("view_idx") -> {
                        kotlin.runCatching {
                            val viewIdxString = json["view_idx"]!!.jsonPrimitive.content
                            ViewIdx.parse(viewIdxString)?.also(viewIdxList::add)
                        }
                    }
                    json is JsonObject -> json.entries
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

