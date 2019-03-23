package com.arsylk.dcwallpaper.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Async.AsyncBanners;
import com.arsylk.dcwallpaper.Async.interfaces.OnBannerPost;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.utils.ViewFactory;
import com.arsylk.dcwallpaper.widgets.dcbanner.DCBannerWidget;

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

        //load up-to-date banners
        new AsyncBanners(context, false).setOnBannerPost(new OnBannerPost() {
            @Override
            public void onProgressUpdate(DCBanners.Banner... banners) {
                if(banners == null) {
                    images_layout.removeAllViews();
                    return;
                }

                for(DCBanners.Banner banner : banners) {
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
        }).execute();
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

