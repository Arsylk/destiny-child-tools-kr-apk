package com.arsylk.mammonsmite.Live2D;

import android.graphics.*;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.arsylk.mammonsmite.utils.Utils;
import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class L2DRenderer implements GLSurfaceView.Renderer {
    private L2DConfig config;
    private L2DModel dcModel;
    private InputStream[] textures;
    private MotionQueueManager motionMgr;
    private Live2DModelAndroid live2DModel;
    private Live2DMotion motionIdle, motionAttack;
    private SimpleImage background;


    private float glWidth = 0.0f, glHeight = 0.0f;
    private float glS = 2.0f;
    private float modelHeight = 0.0f;
    private float modelWidth = 0.0f;
    private float aspect = 0.0f;
    private volatile boolean isLoaded = false;
    private volatile boolean isTextured = false;

    private volatile boolean requestedPreview = false;

    public L2DRenderer(L2DConfig config) {
        this.config = config;
    }

    //OpenGL renderer
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        //all loading in surface changed
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("mTag:Thread", "Surface changed "+ (Thread.currentThread().getName()));
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-glS, glS, glS, -glS, 0.0f, 1.0f);

        glWidth = width;
        glHeight = height;
        aspect = ((float) width) / ((float) height);

        //force square screen to smaller dimension for background
        //if(aspect <= 1) {
        //    gl.glOrthof(-glS, glS, glS/aspect, -glS/aspect, 0.0f, 1.0f);
        //}else {
        //    gl.glOrthof(-glS*aspect, glS*aspect, glS, -glS, 0.0f, 1.0f);
        //}

        if(!isLoaded) {
            asyncLoad();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //return if not loaded
        if(!isLoaded)
            return;

        //reload if requested
        if(config.shouldReload()) {
            asyncLoad();
        }

        //clear gl
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE , GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4f(1, 1, 1, 1);

        //texture background
        if(!background.isTextured()) {
            background.load(gl);
        }

        //texture model
        if(!isTextured) {
            for(int i = 0; i < textures.length; i++) {
                live2DModel.setTexture(i, UtOpenGL.loadTexture(gl, textures[i], true));
                try {
                    textures[i].close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
                textures[i] = null;
            }
            System.gc();
            isTextured = true;
        }

        //create preview if requested
        if(requestedPreview) {
            onDrawPreview(gl);
            return;
        }

        //draw background
        background.draw(gl);

        //moves gl after drawing bg
        gl.glTranslatef(config.getModelOffsetX(), config.getModelOffsetY(), 0.0f);
        //scale gl x, y, z after drawing bg
        gl.glScalef(config.getModelScale(), config.getModelScale(), 0.0f);

        //map gl as a 2d surface
        float glScaledWidth = modelHeight * aspect;
        float glOffsetX = -(glScaledWidth-modelWidth)/2.0f;
        gl.glOrthof(glOffsetX, glScaledWidth+glOffsetX, 0.0f, modelHeight, 0.0f, 1.0f);

        //live2d start
        try{
            if(config.isAnimated()) {
                live2DModel.loadParam();
                if(motionMgr.isFinished()) {
                    motionMgr.startMotion(motionIdle, false);
                }else {
                    motionMgr.updateParam(live2DModel);
                }
                live2DModel.saveParam();
            }

            live2DModel.setGL(gl);
            live2DModel.update();
            live2DModel.draw();
        }catch(Exception e) {
        }
        //live2d end
    }

    //Alternative onDraw
    private void onDrawPreview(GL10 gl) {
        //moves gl
        gl.glTranslatef(config.getModelOffsetX(), config.getModelOffsetY(), 0.0f);
        //scale gl x, y, z
        gl.glScalef(config.getModelScale(), config.getModelScale(), 0.0f);

        //map gl as a 2d surface
        float glScaledWidth = modelHeight * aspect;
        float glOffsetX = -(glScaledWidth-modelWidth)/2.0f;
        gl.glOrthof(glOffsetX, glScaledWidth+glOffsetX, 0.0f, modelHeight, 0.0f, 1.0f);

        live2DModel.init();
        live2DModel.setGL(gl);
        live2DModel.update();
        live2DModel.draw();
        getPreviewFrame();
        requestedPreview = false;
    }


    //OpenGL fileLoad tools
    private synchronized void asyncLoad() {
        asyncLoad(true, true);
    }

    private synchronized void asyncLoad(final boolean loadBackground, final boolean loadModel) {
        Log.d("mTag:Thread", "Re-loading model!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(loadBackground) {
                    loadBackground();
                }
                if(loadModel) {
                    loadModel();
                    isTextured = false;
                }
                isLoaded = true;
            }
        }).start();
    }

    private synchronized void loadModel() {
        Log.d("mTag:Thread", "Model fileLoad start "+ (Thread.currentThread().getName()));
        InputStream in; File motionFile;
        try {
            dcModel = new L2DModel(config.getModelPath());
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            //model
            in = new FileInputStream(dcModel.getCharacter());
            live2DModel = Live2DModelAndroid.loadModel(in);
            in.close();
            //model sizes
            modelWidth = live2DModel.getCanvasWidth();
            modelHeight = live2DModel.getCanvasHeight();
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            //texture
            File[] fileTextures = dcModel.getTextures();
            textures = new InputStream[fileTextures.length];
            for(int i = 0; i < fileTextures.length; i++) {
                textures[i] = new FileInputStream(fileTextures[i]);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            //motion idle
            motionFile = dcModel.getMotion("idle");
            if(motionFile != null) {
                in = new FileInputStream(motionFile);
                motionIdle = Live2DMotion.loadMotion(in);
                in.close();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        try {
            //motion attack
            motionFile = dcModel.getMotion("attack");
            if(motionFile != null) {
                in = new FileInputStream(motionFile);
                motionAttack = Live2DMotion.loadMotion(in);
                in.close();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        motionMgr = new MotionQueueManager();
        if(new File(dcModel.getOutput(), "_preview.png").exists())
            requestedPreview = false;
        Log.d("mTag:Thread", "Model fileLoad end "+ (Thread.currentThread().getName()));
    }

    private synchronized void loadBackground() {
        File bg_file = new File(config.getBackgroundPath());
        float width = 1;
        float height = 1;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if(bg_file.exists()) {
                Bitmap src = BitmapFactory.decodeFile(bg_file.getAbsolutePath());
                src.compress(Bitmap.CompressFormat.PNG, 100, bos);
                width = src.getWidth();
                height = src.getHeight();
                src.recycle();
            }else {
                Bitmap test = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                new Canvas(test).drawColor(Color.WHITE);
                test.compress(Bitmap.CompressFormat.PNG, 0, bos);
                try {
                    FileOutputStream fos = new FileOutputStream(bg_file);
                    bos.writeTo(fos);
                    fos.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
                test.recycle();
            }
            InputStream in = new ByteArrayInputStream(bos.toByteArray());
            bos.close();
            float[] rect = new float[] {-glS, glS, -glS, glS};
            float glRatio, bgRatio;
            if(config.getBgMode() == L2DConfig.BG_MODE_FIT_X) {
                glRatio = glHeight / glWidth;
                bgRatio = height / width;
                rect[2] = rect[0] * bgRatio / glRatio;
                rect[3] = rect[1] * bgRatio / glRatio;
            }else if(config.getBgMode() == L2DConfig.BG_MODE_FIT_Y) {
                glRatio = glHeight / glWidth;
                bgRatio = height / width;
                rect[0] = rect[2] / bgRatio * glRatio;
                rect[1] = rect[3] / bgRatio * glRatio;
            }
            System.out.println(Arrays.toString(rect));
            background = new SimpleImage(in);
            background.setDrawRect(rect[0], rect[1], rect[2], rect[3]);
            background.setUVRect(0.0f,1.0f,1.0f,0.0f);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void loadNewConfig(L2DConfig config) {
        config.requestReload();
        this.config = config;
    }

    //input & controls
    public void startAttackMotion() {
        if(motionAttack != null && motionMgr != null && config.isTappable()) {
            motionMgr.startMotion(motionAttack, true);
        }
    }

    //lifecycle
    public void requestPreview() {
        requestedPreview = true;
    }

    //getters
    public L2DConfig getConfig() {
        return config;
    }

    public boolean isLoadedFully() {
        return isLoaded && isTextured;
    }

    public void getPreviewFrame() {
        ByteBuffer bb = ByteBuffer.allocateDirect((int) ((glWidth*glHeight) * 4));
        bb.order(ByteOrder.nativeOrder());
        bb.position(0);
        GLES10.glReadPixels(0, 0, (int)glWidth, (int)glHeight, GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, bb);
        Bitmap bitmap = Bitmap.createBitmap((int)glWidth, (int)glHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(bb);
        Matrix m = new Matrix();
        m.preScale(1, -1);
        Bitmap inverted = Bitmap.createBitmap(bitmap, 0, 0, (int)glWidth, (int)glHeight, m, false);
        bitmap.recycle();
        bb.clear();

        try {
            Bitmap cut = Utils.trim(inverted);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            cut.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(new File(dcModel.getOutput(), "_preview.png"));
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            bos.close();
            inverted.recycle();
            cut.recycle();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
