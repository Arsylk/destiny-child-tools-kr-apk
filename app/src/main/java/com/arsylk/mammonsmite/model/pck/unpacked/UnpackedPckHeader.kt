package com.arsylk.mammonsmite.model.pck.unpacked

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnpackedPckHeader(
    @SerialName("entries")
    val entries: List<UnpackedPckEntry>
)