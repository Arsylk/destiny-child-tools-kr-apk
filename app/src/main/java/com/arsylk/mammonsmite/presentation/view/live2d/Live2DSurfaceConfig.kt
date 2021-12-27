package com.arsylk.mammonsmite.presentation.view.live2d

import androidx.compose.ui.geometry.Offset
import com.arsylk.mammonsmite.presentation.view.live2d.BackgroundScale.*
import java.io.File
import java.io.Serializable

data class Live2DSurfaceConfig(
    val bgFile: File? = null,
    val bgScale: BackgroundScale = STRETCH,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f,
    val scale: Float = 1.0f,
    val animate: Boolean = true,
    val tappable: Boolean = true,
): Serializable {
    val offset: Offset get() = Offset(offsetX, offsetY)

    companion object {
        val rangeOffsetX = -3.0f..3.0f
        val rangeOffsetY = -3.0f..3.0f
        val rangeScale = 0.1f..6.0f

        val Default = Live2DSurfaceConfig()
    }
}

enum class BackgroundScale(val text: String) {
    STRETCH("Stretch"), FIT_X("Fit X"), FIT_Y("Fit Y")
}