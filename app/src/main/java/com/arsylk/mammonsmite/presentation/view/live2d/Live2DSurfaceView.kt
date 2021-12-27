package com.arsylk.mammonsmite.presentation.view.live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded

class Live2DSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): GLSurfaceView(context, attrs) {
    private val scope: LifecycleCoroutineScope? get() = findViewTreeLifecycleOwner()?.lifecycle?.coroutineScope
    private val renderer: Live2DRenderer = Live2DRenderer { scope }

    var config: Live2DSurfaceConfig
        set(value) { renderer.config = value }
        get() = renderer.config
    var loadedL2dFile: L2DFileLoaded?
        set(value) { renderer.loadedL2dFile = value }
        get() = renderer.loadedL2dFile


    init {
        holder.setFormat(PixelFormat.RGBA_8888)
        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun totsugeki() = renderer.totsugeki()

    suspend fun getPreviewFrame() = renderer.getPreviewFrame()
}