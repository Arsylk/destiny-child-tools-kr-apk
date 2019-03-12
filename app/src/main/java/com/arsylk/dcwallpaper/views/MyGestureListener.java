package com.arsylk.dcwallpaper.views;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private GestureInterface gestureInterface;

    public MyGestureListener(GestureInterface gestureInterface) {
        this.gestureInterface = gestureInterface;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return gestureInterface.onSingleTapConfirmed(event);
    }

    @Override
    public void onLongPress(MotionEvent event) {
        gestureInterface.onLongPress(event);
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        return gestureInterface.onDoubleTap(event);
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        return gestureInterface.onScroll(event1, event2, distanceX, distanceY);
    }

    public interface GestureInterface {
        boolean onSingleTapConfirmed(MotionEvent event);
        void onLongPress(MotionEvent event);
        boolean onDoubleTap(MotionEvent event);
        boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY);
    }
}
