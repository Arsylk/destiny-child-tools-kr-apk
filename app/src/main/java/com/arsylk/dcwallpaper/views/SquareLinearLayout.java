package com.arsylk.dcwallpaper.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.arsylk.dcwallpaper.R;

public class SquareLinearLayout extends LinearLayout {
    private boolean squareWidth = true;

    public SquareLinearLayout(Context context) {
        super(context);
    }

    public SquareLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributeSet(attrs);
    }

    public SquareLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttributeSet(attrs);
    }

    private void applyAttributeSet(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SquareLinearLayout, 0, 0);
        try {
            squareWidth = a.getBoolean(R.styleable.SquareLinearLayout_squareWidth, true);
        }finally {
            a.recycle();
        }
    }

    public void setSquareWidth(boolean squareWidth) {
        this.squareWidth = squareWidth;
    }

    public boolean getSquareWidth() {
        return squareWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(squareWidth ? widthMeasureSpec : heightMeasureSpec, squareWidth ? widthMeasureSpec : heightMeasureSpec);
        setMeasuredDimension(squareWidth ? widthMeasureSpec : heightMeasureSpec, squareWidth ? widthMeasureSpec : heightMeasureSpec);
    }
}
