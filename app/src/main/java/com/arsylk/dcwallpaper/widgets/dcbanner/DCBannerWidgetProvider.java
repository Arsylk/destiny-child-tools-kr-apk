package com.arsylk.dcwallpaper.widgets.dcbanner;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.arsylk.dcwallpaper.Async.AsyncBanners;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;


public class DCBannerWidgetProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String[] enabledBanners = null;
    public DCBanners banners = null;

    public DCBannerWidgetProvider(final Context context, final int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.enabledBanners = Utils.getWidgetPref(context, widgetId, "enabled_banners").split(",");
    }

    @Override
    public void onDataSetChanged() {
        try {
            if(banners == null) {
                new AsyncBanners(context, false).setCallback(new FutureCallback<DCBanners>() {
                    @Override
                    public void onCompleted(Exception e, DCBanners result) {
                        banners = result;
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(widgetId, R.id.dcbanner_view_flipper);
                    }
                }).execute();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log.d("mTag:Factory", "On create Factory service");
    }

    @Override
    public void onDestroy() {
        Log.d("mTag:Factory", "On destroy Factory service");
    }

    @Override
    public int getCount() {
        return enabledBanners != null ? enabledBanners.length : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if(enabledBanners == null || banners == null) return getLoadingView();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner_flip);
        DCBanners.Banner banner = banners.getBanner(Integer.valueOf(enabledBanners[position]));

        views.setTextViewText(R.id.dcbanner_flip_label, banner.getFormattedTimeLeft());
        views.setImageViewBitmap(R.id.dcbanner_flip_image, banner.loadImageBitmap(context));

        Intent intent = new Intent();
        intent.putExtra("URL", banner.getArticleUrl());
        views.setOnClickFillInIntent(R.id.dcbanner_flip_image, intent);

        Log.d("mTag:Factory", "Get View At: "+position+" <> "+banner.getFormattedTimeLeft());
        return views;
    }

    @Override
    public RemoteViews getLoadingView()  {
        RemoteViews loadingViews = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner_flip);
        loadingViews.setImageViewResource(R.id.dcbanner_flip_image, R.drawable.banner_destinychild);

        return loadingViews;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}