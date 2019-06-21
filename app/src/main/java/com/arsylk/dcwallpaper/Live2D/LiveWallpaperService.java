package com.arsylk.dcwallpaper.Live2D;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class LiveWallpaperService extends GLWallpaperService {
    private static LiveWallpaperService instance = null;
    public static LiveWallpaperService getInstance() {
        return instance;
    }

    private Live2DEngine live2DEngine = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public synchronized void requestReload() {
        if(live2DEngine != null) {
            if(live2DEngine.getRenderer() != null) {
                live2DEngine.getRenderer().loadNewConfig(new L2DConfig(getBaseContext(), L2DConfig.MODE_WALLPAPER));
            }
        }
    }

    public Engine onCreateEngine() {
        live2DEngine = new Live2DEngine();
        return live2DEngine;
    }

    public class Live2DEngine extends GLWallpaperService.GLEngine {
        private L2DWallpaperRenderer renderer = null;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            renderer = new L2DWallpaperRenderer(getApplicationContext());
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.startAttackMotion();
                        }
                    });
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            renderer = null;
        }

        public L2DWallpaperRenderer getRenderer() {
            return renderer;
        }
    }
}
