package com.arsylk.mammonsmite.model.file

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface FileType {
    val label: String
    val icon: ImageVector

    interface File : FileType {
        override val label get() = "File"
        override val icon get() = Icons.Default.Description
    }
    interface Folder : FileType {
        override val label get() = "Folder"
        override val icon get() = Icons.Default.Folder
    }
}
object FileTypeFile : FileType.File
object FileTypeFolder : FileType.Folder
