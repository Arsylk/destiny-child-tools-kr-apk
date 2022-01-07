package com.arsylk.mammonsmite.model.file

enum class NewFileError(val label: String) {
    AlreadyExists("Already exists"),
    InvalidName("Invalid name"),
}