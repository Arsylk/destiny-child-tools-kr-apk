package com.arsylk.mammonsmite.model.live2d

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class L2DExpression(
    @SerialName("type")
    val type: String,

    @SerialName("fade_in")
    val fadeIn: Int? = null,

    @SerialName("fade_out")
    val fadeOut: Int? = null,

    @SerialName("params")
    val params: JsonElement? = null
) {

    companion object {
        const val TYPE = "Live2D Expression"
    }
}