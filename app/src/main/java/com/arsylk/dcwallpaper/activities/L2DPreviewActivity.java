package com.arsylk.dcwallpaper.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.Live2D.L2DSurface;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class L2DPreviewActivity extends ActivityWithExceptionRedirect {
    private L2DSurface surface;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Define.REQUEST_WALLPAPER_SET) {
            startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_l2d_preview);
        initViews();
    }

    private void initViews() {
        surface = findViewById(R.id.live2dGL);
        surface.getConfig().setMode(getIntent().getIntExtra("mode", L2DConfig.MODE_PREVIEW));
        surface.setZOrderOnTop(true);
        surface.setOnLoadedListener(new Utils.Callback() {
            @Override
            public void onCall() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progress_loading).setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.progress_loading).setVisibility(View.VISIBLE);
        if(surface != null)
            surface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(surface != null)
            surface.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(surface.getConfig().getMode() == L2DConfig.MODE_PEEK) {
            try {
                File dir = new File(surface.getConfig().getModelPath()).getParentFile();
                if(dir.exists()) {
                    for(File file : dir.listFiles()) {
                        FileUtils.deleteQuietly(file);
                    }
                    dir.delete();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}