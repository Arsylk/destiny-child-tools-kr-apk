package com.arsylk.mammonsmite.model.pck.unpacked

import com.arsylk.mammonsmite.model.pck.PckEntryFileType

open class UnpackedPckEntry(
    val index: Int,
    val filename: String,
    val hashString: String,
) {
    val type: PckEntryFileType = PckEntryFileType.values()
        .firstOrNull { it.extension == filename.substringAfterLast('.') }
        ?: PckEntryFileType.UNKNOWN

    fun copy(
        index: Int = this.index,
        filename: String = this.filename,
        hashString: String = this.hashString,
    ) = UnpackedPckEntry(index = index, filename = filename, hashString = hashString)
}