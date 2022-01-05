package com.arsylk.mammonsmite.presentation.screen.locale.patch

import com.arsylk.mammonsmite.model.destinychild.LocalePatch
import java.io.File

data class PatchItem(
    val source: PatchSource,
    val isLoading: Boolean,
    val patch: LocalePatch? = null,
    val throwable: Throwable? = null,
)

sealed class PatchSource {
    data class Remote(val type: RemoteLocalePatch) : PatchSource()
    data class Local(val file: File, val type: LocalLocalePatch) : PatchSource()

    val label: String by lazy {
        when (this) {
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