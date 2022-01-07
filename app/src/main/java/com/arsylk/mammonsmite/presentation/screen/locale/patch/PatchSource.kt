package com.arsylk.mammonsmite.presentation.screen.locale.patch

import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import java.io.File


sealed class PatchSource {
    object Game : PatchSource()
    data class Remote(val type: RemoteLocalePatch) : PatchSource()
    data class Local(val file: IFile, val type: LocalLocalePatch) : PatchSource()

    val label: String by lazy {
        when (this) {
            Game -> "Game"
            is Local -> type.label
            is Remote -> type.label
        }
    }
}



enum class RemoteLocalePatch(val label: String) {
    ENGLISH("English"),
    RUSSIAN("Russian"),
}

enum class LocalLocalePatch(val label: String) {
    JSON("Json"),
    PCK("Pck"),
}