package com.arsylk.mammonsmite.presentation.dialog.result.file

import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.file.FileType

data class ResultFileItem(
    val label: String,
    val enabled: Boolean,
    val type: FileType,
    val file: IFile,
)