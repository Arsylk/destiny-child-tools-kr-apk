package com.arsylk.mammonsmite.model.destinychild

import com.arsylk.mammonsmite.domain.serializer.LocalePatchFileTypeSerializer
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

    inline val characterNamesFile get() = files["c40e0023a077cb28"]
    inline val skillNamesFile get() = files["f80a001a49cfda65"]
    inline val itemNamesFile get() = files["8c0a00198ad12ee5"]

    operator fun plus(patch: LocalePatch): LocalePatch {
        val base = files.toMutableMap()
        patch.files.forEach { (key, file) ->
            base[key] = base[key]?.run { plus(file) } ?: file
        }
        return copy(files = base.toMap())
    }
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

    operator fun plus(patch: LocalePatchFile): LocalePatchFile {
        return copy(dict = dict + patch.dict)
    }

    @Serializable(with = LocalePatchFileTypeSerializer::class)
    enum class Type(val serialInt: Int) {
        DICT(1),
        TABLE(0),
        UNKNOWN(-1),
    }
}