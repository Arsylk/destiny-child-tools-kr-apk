package com.arsylk.mammonsmite.model.destinychild

import com.arsylk.mammonsmite.Cfg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkillBuffData(
    @SerialName("idx")
    val idx: String,

    @SerialName("logic")
    val logic: String,

    @SerialName("rank")
    val rank: Int,

    @SerialName("type")
    val type: Int,

    @SerialName("category_1")
    val category1: Int,

    @SerialName("category_2")
    val category2: Int,

    @SerialName("category_3")
    val category3: Int,
) {

    val iconUrl get() = "${Cfg.API_URL}static/icons/buff/${idx}.png"
}