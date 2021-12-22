package com.arsylk.mammonsmite.model.pck.unpacked.model

import com.google.gson.annotations.SerializedName

data class ModelPckModelInfo(
    @SerializedName("version")
    val version: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("textures")
    val textures: List<String>,

    @SerializedName("motions")
    val motionMap: Map<String, List<ModelInfoMotion>>,

    @SerializedName("expressions")
    val expressions: List<ModelInfoExpression> = emptyList()
) {

    fun getSortedMotions(): List<Pair<String, ModelInfoMotion>> {
        return motionMap.entries
            .sortedBy { (k, _) -> k.lowercase() }
            .map { (k, v) ->  k to v.first() }
    }
}

data class ModelInfoMotion(
    @SerializedName("file")
    val filename: String,

    @SerializedName("fade_in")
    val fadeIn: Int?,

    @SerializedName("fade_out")
    val fadeOut: Int?,
)

data class ModelInfoExpression(
    @SerializedName("name")
    val name: String,

    @SerializedName("file")
    val filename: String,
)