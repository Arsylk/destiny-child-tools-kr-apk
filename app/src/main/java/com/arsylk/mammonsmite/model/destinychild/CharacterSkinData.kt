package com.arsylk.mammonsmite.model.destinychild

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterSkinData(
    @SerialName("idx")
    val idx: String,
    @SerialName("view_idx")
    val viewIdxList: List<ViewIdx>,
)