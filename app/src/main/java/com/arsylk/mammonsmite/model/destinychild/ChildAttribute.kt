package com.arsylk.mammonsmite.model.destinychild

import androidx.annotation.DrawableRes
import com.arsylk.mammonsmite.R
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChildAttribute(
    @DrawableRes val iconRes: Int,
    @DrawableRes val frameRes: Int,
) {
    @SerialName("0")
    NONE(android.R.color.transparent, android.R.color.transparent),
    @SerialName("1")
    WATER(R.drawable.ic_element_water, R.drawable.frame_element_water),
    @SerialName("2")
    FIRE(R.drawable.ic_element_fire, R.drawable.frame_element_fire),
    @SerialName("3")
    FOREST(R.drawable.ic_element_forest, R.drawable.frame_element_forest),
    @SerialName("4")
    LIGHT(R.drawable.ic_element_light, R.drawable.frame_element_light),
    @SerialName("5")
    DARK(R.drawable.ic_element_dark, R.drawable.frame_element_dark);
}