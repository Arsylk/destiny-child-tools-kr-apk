package com.arsylk.mammonsmite.model.common

import androidx.compose.ui.graphics.Color

data class LogLine(
    val msg: String,
    val tag: String? = null,
    val throwable: Throwable? = null,
    val type: Type = Type.INFO
) {

    enum class Type(val color: Color) {
        INFO(Color.White),
        ERROR(Color.Red),
        SUCCESS(Color.Green),
    }
}