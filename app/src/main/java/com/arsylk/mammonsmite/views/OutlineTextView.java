package com.arsylk.mammonsmite.views;

import android.content.Context;
import android.graphics.Canvas;
import androidx.appcompat.widget.AppCompatTextView;
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

