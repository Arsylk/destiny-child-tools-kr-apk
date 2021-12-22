package com.arsylk.mammonsmite.model.common

import java.io.File

enum class FileType {
    FILE { override fun validate(file: File) = super.validate(file) && file.isFile },
    FOLDER { override fun validate(file: File) = super.validate(file) && file.isDirectory },
    ANY;

    open fun validate(file: File) =  file.exists()
}