package com.arsylk.mammonsmite.model.live2d

import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class L2DHeader(
    @SerialName("filename")
    val modelInfoFilename: String,
    @SerialName("view_idx")
    val viewIdx: ViewIdx? = null,
): java.io.Serializable