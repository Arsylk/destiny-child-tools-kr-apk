package com.arsylk.mammonsmite.model.destinychild

import androidx.annotation.DrawableRes
import com.arsylk.mammonsmite.R
import com.google.gson.annotations.SerializedName

enum class ChildRole(@DrawableRes val iconRes: Int) {
    @SerializedName("0")
    NONE(android.R.color.transparent),
    @SerializedName("1")
    ATTACKER(R.drawable.ic_type_attacker),
    @SerializedName("2")
    TANK(R.drawable.ic_type_tank),
    @SerializedName("3")
    HEALER(R.drawable.ic_type_healer),
    @SerializedName("4")
    DEBUFFER(R.drawable.ic_type_debuffer),
    @SerializedName("5")
    SUPPORT(R.drawable.ic_type_support),
    @SerializedName("6")
    EXP(android.R.color.transparent),
    @SerializedName("7")
    UPGRADE(android.R.color.transparent),
    @SerializedName("8")
    OVER_LIMIT(android.R.color.transparent),
    @SerializedName("9")
    MAX_EXP(android.R.color.transparent);
}