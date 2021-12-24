package com.arsylk.mammonsmite.model.destinychild

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalePatch(
    @SerialName("name")
    val name: String,

    @SerialName("date")
    val date: String?,

    @SerialName("files")
    val files: Map<String, LocalePatchFile>,
) {

    val characterNamesFile get() = files["c40e0023a077cb28"]
}

@Serializable
data class LocalePatchFile(
    @SerialName("hash")
    val hash: String,
    @SerialName("line_type")
    val type: Type = Type.UNKNOWN,
    @SerialName("dict")
    val dict: Map<String, String>,
) {

    @Serializable
    enum class Type {
        @SerialName("1")
        DICT,
        @SerialName("0")
        TABLE,
        UNKNOWN
    }
}