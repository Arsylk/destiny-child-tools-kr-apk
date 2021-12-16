package com.arsylk.mammonsmite.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class ActivityWithExceptionRedirect extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionActivity.RedirectExceptionHandler(this));
        super.onCreate(savedInstanceState);
    }
}
