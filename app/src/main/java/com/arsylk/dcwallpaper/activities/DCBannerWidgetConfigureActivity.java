package com.arsylk.dcwallpaper.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.utils.ViewFactory;
import com.arsylk.dcwallpaper.views.DCBannerWidget;
import com.koushikdutta.async.future.FutureCallback;

public class DCBannerWidgetConfigureActivity extends AppCompatActivity {
    private Context context = DCBannerWidgetConfigureActivity.this;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ViewGroup images_layout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_dcwidget);

        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if(widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            initViews();
        }else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void initViews() {
        images_layout = findViewById(R.id.configure_images_layout);

        findViewById(R.id.configure_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveWidgetConfig();
            }
        });

        //preload saved banners
        DCBanners banners = LoadAssets.getDCBannersInstance();
        if(banners.isFileLoaded() || banners.isWebLoaded()) {
            initBannerViews(banners);
        }

        //load up-to-date banners
        banners.webLoad(context, new FutureCallback<DCBanners>() {
            @Override
            public void onCompleted(Exception e, DCBanners result) {
                if(e == null) {
                    initBannerViews(result);
                    result.loadAllBitmaps(context);
                    result.loadAllArticles(context);
                }else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initBannerViews(DCBanners banners) {
        images_layout.removeAllViews();
        for(DCBanners.Banner banner : banners.getBanners()) {
            ImageView imageView = ViewFactory.getBannerView(context, banner);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setScaleX(view.getScaleX() == 1.0f ? 0.9f : 1.0f);
                    view.setScaleY(view.getScaleY() == 1.0f ? 0.9f : 1.0f);
                }
            });
            images_layout.addView(imageView);
        }
    }

    private void saveWidgetConfig() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < images_layout.getChildCount(); i++) {
            if(images_layout.getChildAt(i).getScaleX() == 1.0f && images_layout.getChildAt(i).getScaleY() == 1.0f) {
                sb.append(i);
                sb.append(",");
            }
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
            Utils.setWidgetPref(context, widgetId, "enabled_banners", sb.toString());
            DCBannerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);
            setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
            finish();
        }else {
            Toast.makeText(context, "Pick a banner first!", Toast.LENGTH_SHORT).show();
        }
    }
}

