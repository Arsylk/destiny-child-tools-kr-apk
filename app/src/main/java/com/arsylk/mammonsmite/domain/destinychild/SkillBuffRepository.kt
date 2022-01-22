package com.arsylk.mammonsmite.domain.destinychild

import com.arsylk.mammonsmite.model.destinychild.FullBuff
import com.arsylk.mammonsmite.model.destinychild.FullSkill
import com.arsylk.mammonsmite.model.destinychild.SkillActiveData
import com.arsylk.mammonsmite.model.destinychild.SkillBuffData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class SkillBuffRepository(
    private val scope: CoroutineScope,
    private val engLocaleRepository: EngLocaleRepository
) {
    private val _skillActiveData = MutableStateFlow(emptyMap<String, SkillActiveData>())
    private val _skillBuffData = MutableStateFlow(emptyMap<String, SkillBuffData>())
    private val _skillCategoryData = MutableStateFlow(emptyMap<Int, String>())
    val skillActiveData by lazy(_skillActiveData::asStateFlow)
    val skillBuffData by lazy(_skillBuffData::asStateFlow)
    val skillCategoryData by lazy(_skillCategoryData::asStateFlow)
    val fullBuffs = combineFullBuffs()
    val fullSkills = combineFullSkills()


    fun setSkillActiveData(data: Map<String, SkillActiveData>) {
        _skillActiveData.value = data
    }

    fun setSkillBuffData(data: Map<String, SkillBuffData>) {
        _skillBuffData.value = data
    }

    fun setSkillCategoryData(data: Map<String, Int>) {
        _skillCategoryData.value = data.map { (text, id) ->
            id to text
        }.toMap()
    }

    private fun combineFullBuffs() =
        combine(engLocaleRepository.engLocale, skillBuffData, skillCategoryData) { locale, buffs, categories ->
            buffs.mapValues { (idx, data) ->
                FullBuff(
                    text = locale?.skillNamesFile?.dict?.get(idx),
                    data = data,
                    categories = data.categories.mapNotNull { i ->
                        categories[i]?.let { i to it }
                    }.toMap()
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private fun combineFullSkills() =
        combine(engLocaleRepository.engLocale, skillActiveData, fullBuffs) { locale, skills, buffs->
            skills.mapValues { (idx, data) ->
                FullSkill(
                    text = locale?.skillNamesFile?.dict?.get(idx),
                    data = data,
                    buffs = data.buffs.mapNotNull { buffIdx ->
                        buffs[buffIdx]?.let { buffIdx to it }
                    }.toMap()
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())
}