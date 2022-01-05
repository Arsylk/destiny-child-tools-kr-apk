package com.arsylk.mammonsmite.model.common

import androidx.compose.ui.graphics.Color

data class LogLine(
    val msg: String,
    val tag: String? = null,
    val throwable: Throwable? = null,
    val type: Type = Type.INFO
) {
    constructor(throwable: Throwable, tag: String? = null, type: Type = Type.ERROR) : this(
        msg = throwable.javaClass.simpleName,
        tag = tag,
        throwable = throwable,
        type = type,
    )

    enum class Type(val color: Color) {
        INFO(Color.White),
        ERROR(Color.Red),
        WARN(Color.Yellow),
        SUCCESS(Color.Green),
    }
}