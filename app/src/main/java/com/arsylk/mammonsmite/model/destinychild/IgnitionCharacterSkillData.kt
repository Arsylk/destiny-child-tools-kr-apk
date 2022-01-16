package com.arsylk.mammonsmite.model.destinychild

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IgnitionCharacterSkillData(

    @SerialName("character_idx")
    val charIdx: String,

    @SerialName("ignition_level")
    val ignitionLevel: Int,

    @SerialName("redstar")
    val redStar: Int,

    @SerialName("skill_1")
    val skill1: String,

    @SerialName("skill_2")
    val skill2: String,

    @SerialName("skill_3")
    val skill3: String,

    @SerialName("skill_4")
    val skill4: String,

    @SerialName("skill_5")
    val skill5: String,
) {

    inline val skills: List<String> get() = listOf(skill1, skill2, skill3, skill4, skill5)
}

typealias IgnitionCharacterSkills = Map<String, Map<Int, IgnitionCharacterSkillData>>