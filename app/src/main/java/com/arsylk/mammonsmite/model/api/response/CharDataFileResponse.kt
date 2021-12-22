package com.arsylk.mammonsmite.model.api.response

import com.arsylk.mammonsmite.model.destinychild.ChildAttribute
import com.arsylk.mammonsmite.model.destinychild.ChildRole
import com.google.gson.annotations.SerializedName

typealias CharDataFileResponse = Map<String, CharData>

data class CharData(
    @SerializedName("idx")
    val idx: String,

    @SerializedName("attribute")
    val attribute: ChildAttribute,

    @SerializedName("role")
    val role: ChildRole,

    @SerializedName("start_grade")
    val stars: Int,

    @SerializedName("name")
    val koreanName: String,

    @SerializedName("skill_1")
    val skill1: String,

    @SerializedName("skill_2")
    val skill2: String,

    @SerializedName("skill_3")
    val skill3: String,

    @SerializedName("skill_4")
    val skill4: String,

    @SerializedName("skill_5")
    val skill5: String,
)