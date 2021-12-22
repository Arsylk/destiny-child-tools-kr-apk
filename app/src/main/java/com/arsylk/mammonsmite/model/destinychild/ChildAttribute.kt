package com.arsylk.mammonsmite.model.destinychild

import androidx.annotation.DrawableRes
import com.arsylk.mammonsmite.R
import com.google.gson.annotations.SerializedName

enum class ChildAttribute(
    @DrawableRes val iconRes: Int,
    @DrawableRes val frameRes: Int,
) {
    @SerializedName("0")
    NONE(android.R.color.transparent, android.R.color.transparent),
    @SerializedName("1")
    WATER(R.drawable.ic_element_water, R.drawable.frame_element_water),
    @SerializedName("2")
    FIRE(R.drawable.ic_element_fire, R.drawable.frame_element_fire),
    @SerializedName("3")
    FOREST(R.drawable.ic_element_forest, R.drawable.frame_element_forest),
    @SerializedName("4")
    LIGHT(R.drawable.ic_element_light, R.drawable.frame_element_light),
    @SerializedName("5")
    DARK(R.drawable.ic_element_dark, R.drawable.frame_element_dark);
}