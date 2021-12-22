package com.arsylk.mammonsmite.model.live2d

import com.google.gson.annotations.SerializedName

data class L2DExpression(
    @SerializedName("type")
    val type: String,

    @SerializedName("fade_in")
    val fadeIn: Int?,

    @SerializedName("fade_out")
    val fadeOut: Int?,
) {

    companion object {
        const val TYPE = "Live2D Expression"
    }
}