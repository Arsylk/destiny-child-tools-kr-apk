package com.arsylk.mammonsmite.model.pck.unpacked

import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import java.io.File
import java.io.Serializable

data class UnpackedPckFile(
    val folder: File,
    val header: UnpackedPckHeader,
): Serializable {
    val headerFile = File(folder, HEADER_FILENAME)

    fun getEntries(type: PckEntryFileType) =
        header.entries.filter { it.type == type }

    fun getEntries(hashStringStart: String) =
        header.entries.filter { it.hashString.startsWith(hashStringStart) }

    fun getEntries(type: PckEntryFileType, hashStringStart: String) =
        header.entries.filter { it.type == type && it.hashString.startsWith(hashStringStart) }

    fun getEntryFile(entry: UnpackedPckEntry) = File(folder, entry.filename)

    companion object {
        const val HEADER_FILENAME = "_header"
    }
}