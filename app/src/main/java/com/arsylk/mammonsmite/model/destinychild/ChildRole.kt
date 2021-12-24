package com.arsylk.mammonsmite.model.destinychild

import androidx.annotation.DrawableRes
import com.arsylk.mammonsmite.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.JsonNames

@Serializable
enum class ChildRole(@DrawableRes val iconRes: Int) {
    @SerialName("0")
    NONE(android.R.color.transparent),
    @SerialName("1")
    ATTACKER(R.drawable.ic_type_attacker),
    @SerialName("2")
    TANK(R.drawable.ic_type_tank),
    @SerialName("3")
    HEALER(R.drawable.ic_type_healer),
    @SerialName("4")
    DEBUFFER(R.drawable.ic_type_debuffer),
    @SerialName("5")
    SUPPORT(R.drawable.ic_type_support),
    @SerialName("6")
    EXP(android.R.color.transparent),
    @SerialName("7")
    UPGRADE(android.R.color.transparent),
    @SerialName("8")
    OVER_LIMIT(android.R.color.transparent),
    @SerialName("9")
    MAX_EXP(android.R.color.transparent);
}