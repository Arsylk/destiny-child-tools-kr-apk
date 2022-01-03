package com.arsylk.mammonsmite.model.pck.unpacked

import com.arsylk.mammonsmite.model.pck.PckEntryFileType
import java.io.File
import java.io.Serializable

data class UnpackedPckFile(
    val folder: File,
    val header: UnpackedPckHeader,
): Serializable {
    val headerFile = File(folder, HEADER_FILENAME)
    val backupFile = prepareBackupFile()

    fun getEntries(type: PckEntryFileType) =
        header.entries.filter { it.type == type }

    fun getEntries(hashStringStart: String) =
        header.entries.filter { it.hashString.startsWith(hashStringStart) }

    fun getEntries(type: PckEntryFileType, hashStringStart: String) =
        header.entries.filter { it.type == type && it.hashString.startsWith(hashStringStart) }

    fun getEntryFile(entry: UnpackedPckEntry) = File(folder, entry.filename)

    private fun prepareBackupFile(): File {
        val path = header.gameRelativePath.substringAfterLast("/")
        val nameRaw = when {
            path.isNotBlank() -> path
            else -> folder.name
        }.replace("\\.pck$".toRegex(), "")
        val name = "_${nameRaw}.pck"
        return File(folder, name)
    }

    companion object {
        const val HEADER_FILENAME = "_header"
    }
}