package com.arsylk.mammonsmite.model.pck.unpacked

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnpackedPckHeader(
    @SerialName("name")
    val name: String = "",
    @SerialName("game_path")
    val gameRelativePath: String = "",
    @SerialName("entries")
    val entries: List<UnpackedPckEntry>
): java.io.Serializable