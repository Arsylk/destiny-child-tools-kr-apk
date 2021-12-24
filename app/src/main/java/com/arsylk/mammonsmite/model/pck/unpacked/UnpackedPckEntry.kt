package com.arsylk.mammonsmite.model.pck.unpacked

import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class UnpackedPckEntry(
    @SerialName("index")
    val index: Int,
    @SerialName("filename")
    val filename: String,
    @SerialName("hash")
    val hashString: String,
) {
    @Transient
    val type: PckEntryFileType = PckEntryFileType.values()
        .firstOrNull { it.extension == filename.substringAfterLast('.') }
        ?: PckEntryFileType.UNKNOWN
}