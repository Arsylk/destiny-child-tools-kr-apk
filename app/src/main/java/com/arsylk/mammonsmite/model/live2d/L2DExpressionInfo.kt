package com.arsylk.mammonsmite.model.live2d

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L2DExpressionInfo(
    @SerialName("name")
    val name: String,

    @SerialName("file")
    val filename: String,
)