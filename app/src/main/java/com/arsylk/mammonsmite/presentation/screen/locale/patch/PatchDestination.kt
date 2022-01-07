package com.arsylk.mammonsmite.presentation.screen.locale.patch

import com.arsylk.mammonsmite.domain.files.IFile

sealed class PatchDestination {
    object Game : PatchDestination()
    data class Json(val file: IFile) : PatchDestination()
    data class Pck(val file: IFile) : PatchDestination()

    val label: String by lazy {
        when (this) {
            Game -> "Game"
            is Json -> LocalLocalePatch.JSON.label
            is Pck -> LocalLocalePatch.PCK.label
        }
    }
}