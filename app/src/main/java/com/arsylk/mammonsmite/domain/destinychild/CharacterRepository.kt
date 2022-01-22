package com.arsylk.mammonsmite.domain.destinychild

import com.arsylk.mammonsmite.model.api.response.CharacterSkinDataFileResponse
import com.arsylk.mammonsmite.model.api.response.parse
import com.arsylk.mammonsmite.model.destinychild.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class CharacterRepository(
    private val scope: CoroutineScope,
    private val engLocaleRepository: EngLocaleRepository,
    private val skillBuffRepository: SkillBuffRepository,
) {
    private val _charData = MutableStateFlow<Map<String, CharData>>(emptyMap())
    private val _characterSkinData = MutableStateFlow<Map<String, List<ViewIdx>>>(emptyMap())
    private val _ignitionCharacterSkillData = MutableStateFlow<IgnitionCharacterSkills>(emptyMap())
    val charData by lazy(_charData::asStateFlow)
    val characterSkinData by lazy(_characterSkinData::asStateFlow)
    val viewIdxNames = prepareViewIdxNames()
    val viewIdxChars = combineIntoCharacters()
    val ignitionCharacterSkillData by lazy(_ignitionCharacterSkillData::asStateFlow)
    val fullCharacters = combineFullCharacter()


    fun setCharData(data: Map<String, CharData>) {
        _charData.value = data
    }

    fun setCharacterSkinData(data: CharacterSkinDataFileResponse) {
        _characterSkinData.value = data.parse()
    }

    fun setIgnitionCharacterSkillData(response: IgnitionCharacterSkills) {
        _ignitionCharacterSkillData.value = response
    }


    private fun prepareViewIdxNames() =
        engLocaleRepository.engLocale.mapLatest { patch ->
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
        combine(charData, characterSkinData) { chars, characterSkinData ->
            characterSkinData
                .mapNotNull { (idx, viewIdxList) ->
                    val char = chars[idx]
                        ?: return@mapNotNull null
                    viewIdxList.map { it to char }
                }
                .flatten()
                .toMap()
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private fun combineFullCharacter(): StateFlow<Map<String, FullCharacter>> =
        combine(
            charData,
            characterSkinData,
            viewIdxNames,
            ignitionCharacterSkillData,
            skillBuffRepository.fullSkills,
        ) { chars, skins, names, igniteData, skills ->

            fun ignitedSkillSet(char: CharData): SkillSet? {
                val ignitions = igniteData[char.idx] ?: return null
                val keys = ignitions.keys.sortedDescending()

                return SkillSet(
                    default = keys.firstNotNullOfOrNull { i ->
                        ignitions[i]?.skill1.takeIf { it != "0" }
                    }?.let(skills::get) ?: FullSkill.Empty,
                    normal = keys.firstNotNullOfOrNull { i ->
                        ignitions[i]?.skill2.takeIf { it != "0" }
                    }?.let(skills::get) ?: FullSkill.Empty,
                    slide = keys.firstNotNullOfOrNull { i ->
                        ignitions[i]?.skill3.takeIf { it != "0" }
                    }?.let(skills::get) ?: FullSkill.Empty,
                    drive = keys.firstNotNullOfOrNull { i ->
                        ignitions[i]?.skill4.takeIf { it != "0" }
                    }?.let(skills::get) ?: FullSkill.Empty,
                    leader = keys.firstNotNullOfOrNull { i ->
                        ignitions[i]?.skill5.takeIf { it != "0" }
                    }?.let(skills::get) ?: FullSkill.Empty,
                ).takeIf { set -> set.skills.any { !it.isEmpty } }
            }

            chars.mapValues { (idx, char) ->
                FullCharacter(
                    data = char,
                    viewIdxNames = skins[idx]
                        .orEmpty()
                        .associateWith(names::get),
                    baseSkills = SkillSet(
                        default = skills[char.skill1] ?: FullSkill.Empty,
                        normal = skills[char.skill2] ?: FullSkill.Empty,
                        slide = skills[char.skill3] ?: FullSkill.Empty,
                        drive = skills[char.skill4] ?: FullSkill.Empty,
                        leader = skills[char.skill5] ?: FullSkill.Empty,
                    ),
                    ignitedSkills = ignitedSkillSet(char)
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())
}