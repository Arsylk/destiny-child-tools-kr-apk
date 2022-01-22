package com.arsylk.mammonsmite.model.destinychild


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharData(
    @SerialName("idx")
    val idx: String,

    @SerialName("attribute")
    val attribute: ChildAttribute,

    @SerialName("role")
    val role: ChildRole,

    @SerialName("start_grade")
    val baseGrade: Int,

    @SerialName("name")
    val koreanName: String,

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

    @SerialName("hp")
    val hp: Int,

    @SerialName("atk")
    val atk: Int,

    @SerialName("def")
    val def: Int,

    @SerialName("agi")
    val agi: Int,

    @SerialName("cri")
    val cri: Int,
) {

    inline val skills: List<String> get() = listOf(skill1, skill2, skill3, skill4, skill5)
}