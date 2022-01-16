package com.arsylk.mammonsmite.domain.repo

import androidx.lifecycle.viewModelScope
import com.arsylk.mammonsmite.domain.retrofit.RetrofitApiService
import com.arsylk.mammonsmite.model.api.response.parse
import com.arsylk.mammonsmite.model.destinychild.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class CharacterRepository(
    private val scope: CoroutineScope,
    private val apiService: RetrofitApiService,
    private val json: Json,
) {
    private val _isLoadingTags = MutableStateFlow(emptySet<String>())
    private val _engLocale = MutableStateFlow<LocalePatch?>(null)
    private val _charData = MutableStateFlow(emptyList<CharData>())
    private val _characterSkinData = MutableStateFlow(emptyList<CharacterSkinData>())
    private val _skillActiveData = MutableStateFlow(emptyMap<String, SkillActiveData>())
    private val _skillBuffData = MutableStateFlow(emptyMap<String, SkillBuffData>())
    private val _ignitionCharacterSkillData = MutableStateFlow<IgnitionCharacterSkills>(emptyMap())
    val isLoading = _isLoadingTags.toIsLoading()
    val engLocale by lazy(_engLocale::asStateFlow)
    val charData by lazy(_charData::asStateFlow)
    val characterSkinData by lazy(_characterSkinData::asStateFlow)
    val viewIdxNames = prepareViewIdxNames()
    val viewIdxChars = combineIntoCharacters()
    val skillActiveData by lazy(_skillActiveData::asStateFlow)
    val skillBuffData by lazy(_skillBuffData::asStateFlow)
    val ignitionCharacterSkillData by lazy(_ignitionCharacterSkillData::asStateFlow)
    val fullCharacters = combineAll()

    @Throws(Throwable::class)
    suspend fun fetchEngLocale() {
        suspendLoading(tag = "locale") {
            val response = apiService.getEnglishPatch()
            _engLocale.value = response
        }
    }

    @Throws(Throwable::class)
    suspend fun fetchCharData() {
        suspendLoading(tag = "char") {
            val response = apiService.getCharDataFile()
            _charData.value = response.values.toList()
        }
    }

    @Throws(Throwable::class)
    suspend fun fetchCharacterSkinData() {
        suspendLoading(tag = "skin") {
            val response = apiService.getCharacterSkinDataFile()
            _characterSkinData.value = response.parse()
        }
    }

    @Throws(Throwable::class)
    suspend fun fetchSkillActiveData() {
        suspendLoading(tag = "skill active") {
            val response = apiService.getSkillActiveDataFile()
            _skillActiveData.value = response
        }
    }

    @Throws(Throwable::class)
    suspend fun fetchSkillBuffData() {
        suspendLoading(tag = "skill buffs") {
            val response = apiService.getSkillBuffDataFile()
            _skillBuffData.value = response
        }
    }

    @Throws(Throwable::class)
    suspend fun fetchIgnitionCharacterSkillData() {
        suspendLoading(tag = "ignition") {
            val response = apiService.getIgnitionCharacterSkillDataFile()
            _ignitionCharacterSkillData.value = response
        }
    }

    @Throws(Throwable::class)
    private suspend fun suspendLoading(tag: String = "tag", block: suspend () -> Unit) {
        _isLoadingTags.update { it + tag }
        val result = kotlin.runCatching { block.invoke() }
        _isLoadingTags.update { it - tag }
        result.getOrThrow()
    }

    private fun Flow<Set<String>>.toIsLoading() =
        mapLatest(Set<String>::isNotEmpty)
            .stateIn(scope, SharingStarted.WhileSubscribed(), false)

    private fun prepareViewIdxNames() = engLocale
        .mapLatest { patch ->
                patch?.characterNamesFile?.dict
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
            }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

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

    private fun combineAll(): StateFlow<Map<String, FullCharacter>> {
        val names = combine(viewIdxNames, engLocale) { p1, p2 -> p1 to p2 }
        val skills = combine(skillActiveData, skillBuffData) { p1, p2 -> p1 to p2 }

        return combine(
            charData,
            characterSkinData,
            names,
            ignitionCharacterSkillData,
            skills,
        ) { charData, skinData, (nameData, localeData), igniteData, (skillData, buffData) ->
            fun skillWithBuffs(idx: String): SkillWithBuffs {
                return SkillWithBuffs(
                    text = localeData?.skillNamesFile?.dict?.get(idx),
                    data = skillData[idx],
                    buffs = skillData[idx]?.buffs
                        ?.mapNotNull(buffData::get)
                        .orEmpty()
                        .map { data ->
                            BuffData(
                                text = localeData?.skillNamesFile?.dict?.get(data.idx),
                                data = data
                            )
                        }
                )
            }

            fun ignitedSkillSet(char: CharData): SkillSet? {
                val ignitions = igniteData[char.idx] ?: return null
                val keys = ignitions.keys.sortedDescending()
                return SkillSet(
                    default = skillWithBuffs(
                        keys.firstNotNullOfOrNull { i ->
                            ignitions[i]?.skill1.takeIf { it != "0" }
                        } ?: char.skill1
                    ),
                    normal = skillWithBuffs(
                        keys.firstNotNullOfOrNull { i ->
                            ignitions[i]?.skill2.takeIf { it != "0" }
                        } ?: char.skill2
                    ),
                    slide = skillWithBuffs(
                        keys.firstNotNullOfOrNull { i ->
                            ignitions[i]?.skill3.takeIf { it != "0" }
                        } ?: char.skill3
                    ),
                    drive = skillWithBuffs(
                        keys.firstNotNullOfOrNull { i ->
                            ignitions[i]?.skill4.takeIf { it != "0" }
                        } ?: char.skill4
                    ),
                    leader = skillWithBuffs(
                        keys.firstNotNullOfOrNull { i ->
                            ignitions[i]?.skill5.takeIf { it != "0" }
                        } ?: char.skill5
                    ),
                )
            }

            charData.associate { char ->
                char.idx to FullCharacter(
                    data = char,
                    viewIdxNames = skinData
                        .filter { it.idx == char.idx }
                        .flatMap { it.viewIdxList }
                        .associateWith(nameData::get),
                    baseSkills = SkillSet(
                        default = skillWithBuffs(char.skill1),
                        normal = skillWithBuffs(char.skill2),
                        slide = skillWithBuffs(char.skill3),
                        drive = skillWithBuffs(char.skill4),
                        leader = skillWithBuffs(char.skill5),
                    ),
                    ignitedSkills = ignitedSkillSet(char)
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyMap())
    }
}