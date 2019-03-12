package com.arsylk.dcwallpaper.Live2D;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.*;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.views.MyGestureListener;
import com.arsylk.dcwallpaper.views.MyScaleGestureListener;
import com.arsylk.dcwallpaper.views.SaveConfigDialog;
import com.arsylk.dcwallpaper.views.SaveModelDialog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.File;

public class L2DSurface extends GLSurfaceView implements GLSurfaceView.Renderer,
        MyGestureListener.GestureInterface, MyScaleGestureListener.ScaleGestureInterface {
    private volatile L2DRenderer renderer = null;
    private volatile L2DConfig config = null;
    private Utils.Callback callback = null;
    private boolean isLoaded = false, isFullyLoaded = false;
    private int width = 0, height = 0;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;
    private long eventStartTime = 0L;
    private float anchorDistanceX = 0.0f, anchorDistanceY = 0.0f;

    //constructors
    public L2DSurface(Context context) {
        super(context);
        setupRenderer();
    }

    public L2DSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupRenderer();
    }

    private void setupRenderer() {
        config = new L2DConfig(getContext(), L2DConfig.MODE_PREVIEW,
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString("preview_model", ""));
        gestureDetector = new GestureDetector(getContext(), new MyGestureListener(this));
        scaleDetector = new ScaleGestureDetector(getContext(), new MyScaleGestureListener(this));

        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    //lifecycle TODO look at this
    @Override
    public void onPause() {
        super.onPause();
        renderer = null;
        isLoaded = false;
        isFullyLoaded = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //OpenGL renderer
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.width = width;
        this.height = height;
        if(renderer != null) {
            renderer.onSurfaceChanged(gl10, width, height);
        }else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    renderer = new L2DRenderer(config);
                    renderer.requestPreview();
                }
            }).start();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if(renderer != null) {
            if(isLoaded) {
                renderer.onDrawFrame(gl10);
                if(renderer.isLoadedFully() && !isFullyLoaded) {
                    if(callback != null) {
                        isFullyLoaded = true;
                        callback.onCall();
                    }
                }
            }else {
                isLoaded = true;
                onSurfaceChanged(gl10, width, height);
            }
        }
    }

    //input & controls
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        return true;
    }

    //gesture detectors
    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        if(config.isAnimated()) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if(getRenderer() != null)
                        getRenderer().startAttackMotion();
                }
            });
        }
        if(config.isSounds()) {
            String modelId = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("model_id", "");
            File testSound = new File(DCTools.getDCSoundsPath(), modelId+"_drive.ogg");
            if(!testSound.exists()) {
                testSound = new File(DCTools.getDCSoundsPath(), modelId.substring(0, modelId.indexOf("_"))+"_drive.ogg");
            }
            if(testSound.exists()) {
                Utils.playSoundFile(getContext(), testSound);
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        config.setAnimated(!config.isAnimated());
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        if(config.getMode() == L2DConfig.MODE_PEEK) {
            SaveModelDialog saveModelDialog = new SaveModelDialog(getContext(), new L2DModel(config.getModelPath()));
            saveModelDialog.setOnModelSavedListener(new Utils.Callback() {
                @Override
                public void onCall() {
                    if(getContext() instanceof Activity) {
                        ((Activity) getContext()).finish();
                    }
                }
            });
            saveModelDialog.showDialog();
        }else if(config.getMode() == L2DConfig.MODE_PREVIEW) {
            new SaveConfigDialog(getContext(), config).showDialog();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        float matrixX = (4.0f/(float)getMeasuredWidth())*event2.getX()-2.0f;
        float matrixY = (4.0f/(float)getMeasuredHeight())*event2.getY()-2.0f;
        if(event1.getEventTime() != eventStartTime) {
            eventStartTime = event1.getEventTime();
            anchorDistanceX = config.getModelOffsetX() - matrixX;
            anchorDistanceY = config.getModelOffsetY() - matrixY;
        }else {
            float matrixScale = Math.max(2.0f+config.getModelScale()-1.0f, 2.0f);
            matrixX = Math.min(Math.max(matrixX + anchorDistanceX, -matrixScale), matrixScale);
            matrixY = Math.min(Math.max(matrixY + anchorDistanceY, -matrixScale), matrixScale);
            config.setModelOffsetX(matrixX);
            config.setModelOffsetY(matrixY);
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        config.setModelScale(config.getModelScale()*detector.getScaleFactor());
        return true;
    }

    //setters & getters
    public void setOnLoadedListener(Utils.Callback callback) {
        this.callback = callback;
        if(renderer != null) {
            if(renderer.isLoadedFully()) {
                callback.onCall();
            }
        }
    }

    public L2DConfig getConfig() {
        return config;
    }

    public L2DRenderer getRenderer() {
        return renderer;
    }
}
