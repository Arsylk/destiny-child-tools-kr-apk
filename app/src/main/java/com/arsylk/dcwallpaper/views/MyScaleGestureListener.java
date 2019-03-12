package com.arsylk.dcwallpaper.views;

import android.view.ScaleGestureDetector;

public class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private ScaleGestureInterface scaleInterface;

    public MyScaleGestureListener(ScaleGestureInterface scaleInterface) {
        this.scaleInterface = scaleInterface;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return scaleInterface.onScale(detector);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    public interface ScaleGestureInterface {
        boolean onScale(ScaleGestureDetector detector);
    }
}
