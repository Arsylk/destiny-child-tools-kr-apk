package com.arsylk.mammonsmite.presentation.view.live2d

import android.graphics.*
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import androidx.compose.ui.geometry.Offset
import com.arsylk.mammonsmite.Live2D.SimpleImage
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import com.arsylk.mammonsmite.utils.Utils
import jp.live2d.android.Live2DModelAndroid
import jp.live2d.android.UtOpenGL
import jp.live2d.motion.Live2DMotion
import jp.live2d.motion.MotionQueueManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class Live2DRenderer(private val scopeProvider: () -> CoroutineScope?) : GLSurfaceView.Renderer {
    val surfaceSize = MutableStateFlow(0.0f to 0.0f)
    val surfaceAspect: Float get() {
        val (w, h) = surfaceSize.value
        return w / h
    }

    private val motionManager = MotionQueueManager()
    private var live2dModel: Live2DModelAndroid? = null
    private var modelHeight: Float = 0.0f
    private var modelWidth: Float = 0.0f
    private var motionIdle: Live2DMotion? = null
    private var motionAttack: Live2DMotion? = null
    private var modelJob: Job? = null
    private val isModelTextured = AtomicBoolean(false)
    private val isPreviewRequested = AtomicBoolean(false)
    private val previewBitmapChannel = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)

    private var background: SimpleImage? = null
    private var backgroundJob: Job? = null


    var config: Live2DSurfaceConfig = Live2DSurfaceConfig.Default
        set(value) {
            val old = field
            field = value
            if (old.bgFile != value.bgFile || old.bgScale != value.bgScale)
                enqueueBackgroundLoading()
        }
    var loadedL2dFile: L2DFileLoaded? = null
        set(value) {
            val old = field
            field = value
            if(value != null && old != field)
                enqueueModelLoading()
        }


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceSize.value = width.toFloat() to height.toFloat()

        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        gl.glOrthof(-GL_SCALE, GL_SCALE, GL_SCALE, -GL_SCALE, 0.0f, 1.0f)

    }

    override fun onDrawFrame(gl: GL10) {
        //if (isLoading) return

        //clear gl
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glDisable(GL10.GL_DEPTH_TEST)
        gl.glDisable(GL10.GL_CULL_FACE)
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE , GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        // draw preview if requested
        live2dModel?.also {
            if (isModelTextured.get() && isPreviewRequested.get()) {
                isPreviewRequested.set(false)
                return onDrawPreviewFrame(gl, it)
            }
        }

        // load & draw background texture
        background?.run { if (isTextured) draw(gl) else load(gl) }

        // load & draw model textures
        use(live2dModel, loadedL2dFile) { model, l2dFile ->
            if (!isModelTextured.get()) {
                l2dFile.textureFiles.forEachIndexed { i, file ->
                    kotlin.runCatching {
                        file.inputStream().use {
                            model.setTexture(i, UtOpenGL.loadTexture(gl, it, true))
                        }
                    }
                }
                isModelTextured.set(true)
            }
        }
        if (!isModelTextured.get()) return


        // live2d start
        setupGlForLive2D(gl)
        live2dModel?.runCatching {
            if (config.animate) {
                loadParam()
                if (motionManager.isFinished) motionManager.startMotion(motionIdle, false)
                else motionManager.updateParam(this)
                saveParam()
            }

            setGL(gl)
            update()
            draw()
        }
    }

    private fun onDrawPreviewFrame(gl: GL10, live2dModel: Live2DModelAndroid) {
        setupGlForLive2D(gl)
        live2dModel.runCatching {
            setGL(gl)
            update()
            draw()

            val (surfaceWidth, surfaceHeight) = surfaceSize.value
            val bb = ByteBuffer.allocateDirect(((surfaceWidth * surfaceHeight) * 4).toInt())
            bb.order(ByteOrder.nativeOrder())
            bb.position(0)
            GLES10.glReadPixels(0, 0, surfaceWidth.toInt(), surfaceHeight.toInt(), GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, bb)

            val bitmap = Bitmap.createBitmap(surfaceWidth.toInt(), surfaceHeight.toInt(), Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(bb)
            bb.clear()

            previewBitmapChannel.trySend(bitmap)
        }
    }

    private fun setupGlForLive2D(gl: GL10) {
        //move gl after drawing bg
        gl.glTranslatef(config.offsetX, config.offsetY, 0.0f)
        //scale gl x, y, z after drawing bg
        gl.glScalef(config.scale, config.scale, 0.0f)

        //map gl as a 2d surface
        val glScaledWidth = modelHeight * surfaceAspect
        val glOffsetX = -(glScaledWidth - modelWidth) / 2.0f
        gl.glOrthof(glOffsetX, glScaledWidth + glOffsetX, 0.0f, modelHeight, 0.0f, 1.0f)
    }


    fun totsugeki() {
        live2dModel ?: return
        if (config.animate && config.tappable)
            motionAttack?.also { motionManager.startMotion(it, true) }
    }

    suspend fun getPreviewFrame(): Bitmap {
        isPreviewRequested.set(true)
        // await preview bitmap
        val bitmap = previewBitmapChannel.receive()
        val m = Matrix().apply { preScale(1.0f, -1.0f) }
        val inverted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, false)
        if (bitmap != inverted) bitmap.recycle()
        val cut = Utils.trim(inverted)
        if (inverted != cut) inverted.recycle()

        return cut
    }


    private fun enqueueModelLoading() {
        val l2dFile = loadedL2dFile ?: return
        modelJob?.run { if (isActive) cancel() }
        modelJob = scopeProvider.invoke()?.launch(Dispatchers.IO) {
            // try load character dat
            try {
                FileInputStream(l2dFile.characterFile).use {
                    val model = Live2DModelAndroid.loadModel(it)
                    live2dModel = model
                    modelWidth = model.canvasWidth
                    modelHeight = model.canvasHeight
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            // try load idle motions
            try {
                val file = l2dFile.motionIdleFile
                if (file != null) {
                    val mtnStr = buildString {
                        file.useLines(Charset.forName("utf-8")) {
                            for (line in it) {
                                if (line.startsWith("\$tag:")) continue
                                append("$line\n")
                            }
                        }
                    }
                    motionIdle = Live2DMotion.loadMotion(mtnStr.toByteArray())
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            // try load attack motions
            try {
                val file = l2dFile.motionAttackFile
                if (file != null) {
                    val mtnStr = buildString {
                        file.useLines(Charset.forName("utf-8")) {
                            for (line in it) {
                                if (line.startsWith("\$tag:")) continue
                                append("$line\n")
                            }
                        }
                    }
                    motionAttack = Live2DMotion.loadMotion(mtnStr.toByteArray())
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            // reset model texture status
            isModelTextured.set(false)
        }

    }

    private fun enqueueBackgroundLoading() {
        val file = config.bgFile ?: return
        backgroundJob?.run { if (isActive) cancel() }
        backgroundJob = scopeProvider.invoke()?.launch {
            val (surfaceWidth, surfaceHeight) = surfaceSize
                .first { (w, h) -> w != 0.0f && h != 0.0f }
            withContext(Dispatchers.IO) {
                val bos = ByteArrayOutputStream()
                try {
                    val (width, height) = kotlin.runCatching {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                        (bitmap.width to bitmap.height).also { bitmap.recycle() }
                    }.getOrElse {
                        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
                        (bitmap.width to bitmap.height).also { bitmap.recycle() }
                    }

                    val rect = floatArrayOf(-GL_SCALE, GL_SCALE, -GL_SCALE, GL_SCALE)
                    val surfaceRatio = surfaceHeight / surfaceWidth
                    val backgroundRation = height.toFloat() / width.toFloat()
                    when (config.bgScale) {
                        BackgroundScale.FIT_X -> {
                            rect[2] = rect[0] * backgroundRation / surfaceRatio
                            rect[3] = rect[1] * backgroundRation / surfaceRatio
                        }
                        BackgroundScale.FIT_Y -> {
                            rect[0] = rect[2] / backgroundRation * surfaceRatio
                            rect[1] = rect[3] / backgroundRation * surfaceRatio
                        }
                        else -> {}
                    }

                    val ins = ByteArrayInputStream(bos.toByteArray())
                    background = SimpleImage(ins).apply {
                        setDrawRect(rect[0], rect[1], rect[2], rect[3])
                        setUVRect(0.0f,1.0f,1.0f,0.0f)
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                } finally {
                    bos.runCatching(ByteArrayOutputStream::close)
                }
            }
        }
    }

    companion object {

        const val GL_SCALE = 2.0f

        fun matrix(w: Int, h: Int, raw: Offset): Offset {
            return Offset(
                x = ((GL_SCALE * 2) / w) * raw.x - GL_SCALE,
                y = ((GL_SCALE * 2) / h) * raw.y - GL_SCALE
            )
        }

        fun change(anchor: Offset, offset: Offset): Offset {
            val anchorN = anchor - Offset(GL_SCALE, GL_SCALE)
            val offsetN = offset - Offset(GL_SCALE, GL_SCALE)
            return offsetN - anchorN
        }
    }
}