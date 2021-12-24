package com.arsylk.mammonsmite.model.live2d

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L2DMotionInfo(
    @SerialName("file")
    val filename: String,

    @SerialName("fade_in")
    val fadeIn: Int?,

    @SerialName("fade_out")
    val fadeOut: Int?,
)