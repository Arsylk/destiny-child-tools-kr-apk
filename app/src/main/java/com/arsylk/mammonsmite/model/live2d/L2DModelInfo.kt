package com.arsylk.mammonsmite.model.live2d

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L2DModelInfo(
    @SerialName("version")
    val version: String,

    @SerialName("model")
    val model: String,

    @SerialName("textures")
    val textures: List<String>,

    @SerialName("motions")
    val motionMap: Map<String, List<L2DMotionInfo>> = emptyMap(),

    @SerialName("expressions")
    val expressions: List<L2DExpressionInfo> = emptyList()
) {

    fun getSortedMotions(): List<Pair<String, L2DMotionInfo>> {
        return motionMap.entries
            .sortedBy { (k, _) -> k.lowercase() }
            .map { (k, v) ->  k to v.first() }
    }
}

