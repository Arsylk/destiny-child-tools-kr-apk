package com.arsylk.dcwallpaper.views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class OutlineTextView extends AppCompatTextView {
    public OutlineTextView(Context context) {
        super(context);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        for (int i = 0; i < 10; i++) {
            super.draw(canvas);
        }
    }

}

