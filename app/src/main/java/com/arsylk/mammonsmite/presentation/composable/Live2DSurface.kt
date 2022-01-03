package com.arsylk.mammonsmite.presentation.composable

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DRenderer
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceConfig
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceView
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream


@Composable
fun Live2DSurface(
    loadedL2dFile: L2DFileLoaded?,
    surfaceConfig: Live2DSurfaceConfig,
    playMotion: Boolean,
    onMotionPlayed: () -> Unit,
    onGestureTransform: (offset: Offset, scale: Float) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val (mWidth, mHeight) = constraints.maxWidth to constraints.maxHeight
        if (loadedL2dFile != null) {
            var requestPreview by remember(loadedL2dFile) {
                mutableStateOf(!loadedL2dFile.l2dFile.previewFile.exists())
            }
            AndroidView(
                factory = { Live2DSurfaceView(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val mW = (Live2DRenderer.GL_SCALE * 2) * (pan.x / mWidth.toFloat())
                            val mH = (Live2DRenderer.GL_SCALE * 2) * (pan.y / mHeight.toFloat())
                            onGestureTransform(Offset(mW, mH), zoom)
                        }
                    },
                update = { surface ->
                    surface.loadedL2dFile = loadedL2dFile
                    surface.config = surfaceConfig
                    if (playMotion) {
                        surface.totsugeki()
                        onMotionPlayed.invoke()
                    }
                    if (requestPreview) {
                        requestPreview = false
                        scope.launch(NonCancellable) {
                            kotlin.runCatching {
                                val b = surface.getPreviewFrame()
                                FileOutputStream(loadedL2dFile.l2dFile.previewFile).use {
                                    b.compress(Bitmap.CompressFormat.PNG, 95, it)
                                }
                            }
                        }
                    }
                },
            )
        }
    }
}