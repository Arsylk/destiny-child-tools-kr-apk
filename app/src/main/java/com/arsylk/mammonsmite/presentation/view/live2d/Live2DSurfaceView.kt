package com.arsylk.mammonsmite.presentation.view.live2d

import android.content.Context
import android.graphics.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.arsylk.mammonsmite.Live2D.SimpleImage
import com.arsylk.mammonsmite.domain.use
import com.arsylk.mammonsmite.model.live2d.L2DFileLoaded
import jp.live2d.android.Live2DModelAndroid
import jp.live2d.android.UtOpenGL
import jp.live2d.motion.Live2DMotion
import jp.live2d.motion.MotionQueueManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Live2DSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): GLSurfaceView(context, attrs), GLSurfaceView.Renderer {
    private val scope: LifecycleCoroutineScope? get() = findViewTreeLifecycleOwner()?.lifecycle?.coroutineScope
    private val surfaceSize = MutableStateFlow(0.0f to 0.0f)
    private val surfaceAspect: Float get() {
        val (w, h) = surfaceSize.value
        return w / h
    }

    var config: Live2DSurfaceConfig = Live2DSurfaceConfig()
    var backgroundFile: File? = null
        set(value) { field = value; if(value != null) enqueueBackgroundLoading() }
    var loadedL2dFile: L2DFileLoaded? = null
        set(value) { field = value; if(value != null) enqueueModelLoading()}

    private val motionManager = MotionQueueManager()
    private var live2dModel: Live2DModelAndroid? = null
    private var motionIdle: Live2DMotion? = null
    private var motionAttack: Live2DMotion? = null
    private var modelHeight: Float = 0.0f
    private var modelWidth: Float = 0.0f
    private var background: SimpleImage? = null

    private var backgroundJob: Job? = null
    private var modelJob: Job? = null
    private val isModelTextured = AtomicBoolean(false)


    init {
        holder.setFormat(PixelFormat.RGBA_8888)
        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
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

        // load & draw background texture
         background?.run { if (isTextured) draw(gl) else load(gl) }

        // load & draw model textures
        use(live2dModel, loadedL2dFile) { model, l2dFile ->
            if (!isModelTextured.get()) {
                l2dFile.textureFiles.forEachIndexed { i, file ->
                    kotlin.runCatching {
                        file.inputStream().use {
                            live2dModel?.setTexture(i, UtOpenGL.loadTexture(gl, it, true))
                        }
                    }
                }
                isModelTextured.set(true)
            }
        }


        //move gl after drawing bg
        gl.glTranslatef(config.offsetX, config.offsetY, 0.0f)
        //scale gl x, y, z after drawing bg
        gl.glScalef(config.scale, config.scale, 0.0f)

        //map gl as a 2d surface
        val glScaledWidth: Float = modelHeight * surfaceAspect
        val glOffsetX: Float = -(glScaledWidth - modelWidth) / 2.0f
        gl.glOrthof(glOffsetX, glScaledWidth + glOffsetX, 0.0f, modelHeight, 0.0f, 1.0f)

        // live2d start
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


    private fun enqueueBackgroundLoading() {
        println("enqueueBackgroundLoading: $backgroundFile")
        val file = backgroundFile ?: return
        backgroundJob?.run { if (isActive) cancel() }
        backgroundJob = scope?.launchWhenResumed {
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
                        canvas.drawColor(Color.RED)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
                        (bitmap.width to bitmap.height).also { bitmap.recycle() }
                    }

                    val rect = floatArrayOf(-GL_SCALE, GL_SCALE, -GL_SCALE, GL_SCALE)
                    val surfaceRatio = surfaceHeight / surfaceWidth
                    val backgroundRation = height.toFloat() / width.toFloat()
                    when (config.backgroundScale) {
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

    private fun enqueueModelLoading() {
        println("enqueueModelLoading: $loadedL2dFile")
        val l2dFile = loadedL2dFile ?: return
        modelJob?.run { if (isActive) cancel() }
        modelJob = scope?.launchWhenResumed {
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
            // try load motions
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
        }

    }

    companion object {

        const val GL_SCALE = 2.0f
    }
}