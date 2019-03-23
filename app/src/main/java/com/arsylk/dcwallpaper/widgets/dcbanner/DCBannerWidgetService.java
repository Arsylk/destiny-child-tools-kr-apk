package com.arsylk.dcwallpaper.widgets.dcbanner;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;
import com.arsylk.dcwallpaper.widgets.dcbanner.DCBannerWidgetProvider;

public class DCBannerWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("mTag:Factory", "On get View Factory");
        return new DCBannerWidgetProvider(getApplicationContext(), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
    }
}