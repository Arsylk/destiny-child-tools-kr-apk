package com.arsylk.dcwallpaper.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;

public class DCBannerWidget extends AppWidgetProvider {
    public static final String ACTION_REMOTE_FACTORY = "ACTION_REMOTE_FACTORY";

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner);
        Intent intent = new Intent(context, DCBannerWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.dcbanner_view_flipper, intent);

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] widgetIds) {
        for(int widgetId : widgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("mTag:Receive", "On receive: "+intent.getAction());
        if(intent.getAction().equals(ACTION_REMOTE_FACTORY)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateAppWidget(context, appWidgetManager, widgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.dcbanner_view_flipper);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            Utils.removeWidgetPref(context, appWidgetId);
        }
    }
}

