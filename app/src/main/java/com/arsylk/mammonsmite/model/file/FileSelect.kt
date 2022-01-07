package com.arsylk.mammonsmite.model.file

import com.arsylk.mammonsmite.domain.files.IFile

enum class FileSelect {
    FILE { override fun validate(file: IFile) = file.isFile },
    FOLDER { override fun validate(file: IFile) = file.isDirectory },
    ANY;

    open fun validate(file: IFile) = true
}

