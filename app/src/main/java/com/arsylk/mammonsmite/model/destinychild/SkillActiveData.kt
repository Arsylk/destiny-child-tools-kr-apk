package com.arsylk.mammonsmite.model.destinychild

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillActiveData(
    @SerialName("idx")
    val idx: String,

    @SerialName("attribute")
    val attribute: Int,

    @SerialName("buff_1")
    val buff1: String,

    @SerialName("buff_2")
    val buff2: String,

    @SerialName("buff_3")
    val buff3: String,

    @SerialName("buff_4")
    val buff4: String,

    @SerialName("buff_apply")
    val buffApply: Int,

    @SerialName("cost")
    val cost: Int,

    @SerialName("duration_1")
    val duration1: Int,
    @SerialName("duration_1_time")
    val duration1Time: Int,
    @SerialName("duration_1_type")
    val duration1Type: Int,

    @SerialName("duration_2")
    val duration2: Int,
    @SerialName("duration_2_time")
    val duration2Time: Int,
    @SerialName("duration_2_type")
    val duration2Type: Int,

    @SerialName("duration_3")
    val duration3: Int,
    @SerialName("duration_3_time")
    val duration3Time: Int,
    @SerialName("duration_3_type")
    val duration3Type: Int,

    @SerialName("duration_4")
    val duration4: Int,
    @SerialName("duration_4_time")
    val duration4Time: Int,
    @SerialName("duration_4_type")
    val duration4Type: Int,

    @SerialName("gauge_per_sec")
    val gaugePerSec: Int,

    @SerialName("level_equation")
    val levelEquation: Int,
    @SerialName("level_equation_1")
    val levelEquation1: Int,
    @SerialName("level_equation_2")
    val levelEquation2: Int,
    @SerialName("level_equation_3")
    val levelEquation3: Int,
    @SerialName("level_equation_4")
    val levelEquation4: Int,

    @SerialName("n_attack")
    val nAttack: Int,
    @SerialName("n_attack_1")
    val nAttack1: Int,
    @SerialName("n_attack_2")
    val nAttack2: Int,
    @SerialName("n_attack_3")
    val nAttack3: Int,
    @SerialName("n_attack_4")
    val nAttack4: Int,

    @SerialName("target")
    val target: Int,
    @SerialName("target_1")
    val target1: Int,
    @SerialName("target_2")
    val target2: Int,
    @SerialName("target_3")
    val target3: Int,
    @SerialName("target_4")
    val target4: Int,

    @SerialName("target_fit")
    val targetFit: Int,
    @SerialName("target_fit_1")
    val targetFit1: Int,
    @SerialName("target_fit_2")
    val targetFit2: Int,
    @SerialName("target_fit_3")
    val targetFit3: Int,
    @SerialName("target_fit_4")
    val targetFit4: Int,

    @SerialName("time_cool")
    val timeCool: Int,

    @SerialName("type")
    val type: Int,

    @SerialName("value")
    val value: Int,
    @SerialName("value_type")
    val valueType: Int,

    @SerialName("value_1")
    val value1: Int,
    @SerialName("value_1_type")
    val value1Type: Int,

    @SerialName("value_2")
    val value2: Int,
    @SerialName("value_2_type")
    val value2Type: Int,

    @SerialName("value_3")
    val value3: Int,
    @SerialName("value_3_type")
    val value3Type: Int,

    @SerialName("value_4")
    val value4: Int,
    @SerialName("value_4_type")
    val value4Type: Int,
) {

    inline val buffs: List<String> get() = listOf(buff1, buff2, buff3, buff4)
}