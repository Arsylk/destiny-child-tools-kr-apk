package com.arsylk.mammonsmite.presentation.view.live2d

import com.arsylk.mammonsmite.presentation.view.live2d.BackgroundScale.*

data class Live2DSurfaceConfig(
    val backgroundScale: BackgroundScale = XY,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f,
    val scale: Float = 1.0f,
    val animate: Boolean = true,
)

enum class BackgroundScale {
    XY, FIT_X, FIT_Y
}