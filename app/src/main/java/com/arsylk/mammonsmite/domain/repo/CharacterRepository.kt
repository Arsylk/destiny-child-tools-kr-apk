package com.arsylk.mammonsmite.domain.repo

import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.model.api.response.CharData
import com.arsylk.mammonsmite.model.api.response.CharacterSkinData
import com.arsylk.mammonsmite.model.api.response.parse
import com.arsylk.mammonsmite.model.destinychild.DestinyChildCharacter
import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class CharacterRepository(
    private val scope: CoroutineScope,
    private val apiService: RetrofitApiService,
) {
    private val _charData = MutableStateFlow(emptyList<CharData>())
    private val _characterSkinData = MutableStateFlow(emptyList<CharacterSkinData>())
    private val _viewIdxNames = MutableStateFlow(emptyMap<ViewIdx, String>())
    val charData by lazy(_charData::asStateFlow)
    val characterSkinData by lazy(_characterSkinData::asStateFlow)
    val viewIdxNames by lazy(_viewIdxNames::asStateFlow)
    val viewIdxChars = combineIntoCharacters()


    suspend fun fetchAll() {
        println("fetching all ...")
        fetchCharData()
        fetchCharacterSkinData()
        fetchViewIdxNames()
    }

    suspend fun fetchCharData() {
        kotlin.runCatching {
            val response = apiService.getCharDataFile()
            _charData.value = response.map { it.value }
        }
    }

    suspend fun fetchCharacterSkinData() {
        kotlin.runCatching {
            val response = apiService.getCharacterSkinDataFile()
            _characterSkinData.value = response.parse().also { println(it) }
        }.getOrElse { println(it) }
    }

    suspend fun fetchViewIdxNames() {
        kotlin.runCatching {
            val response = apiService.getEnglishPatch()
            val patchFile = response.parse().characterNamesFile
            val entries = patchFile?.dict
                ?.mapNotNull { (k, v) ->
                    val viewIdx = ViewIdx.parse(k) ?: return@mapNotNull null
                    val name = v.substringBefore('\t')
                        .trim().trim('\t')
                        .split("_")
                        .joinToString(" ") { part ->
                            part.trim().trim('\t')
                                .split(" ")
                                .joinToString(separator = " ") {
                                    it.replaceFirstChar { c -> c.uppercase() }
                                        .trim().trim('\t')
                                }
                        }
                        .trim().trim('\t')
                        .takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null

                    return@mapNotNull viewIdx to name
                }.orEmpty()
                .associate { it }
            _viewIdxNames.value = entries
        }
    }

    private fun combineIntoCharacters() =
        combine(charData, characterSkinData) { charData, characterSkinData ->
            characterSkinData
                .mapNotNull { skinData ->
                    val data = charData
                        .firstOrNull { it.idx == skinData.idx }
                        ?: return@mapNotNull null
                    skinData.viewIdxList.map { it to data }
                }
                .flatten()
                .toMap()
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())
}