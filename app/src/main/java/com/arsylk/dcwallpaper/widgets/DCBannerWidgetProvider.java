package com.arsylk.dcwallpaper.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;

import static com.arsylk.dcwallpaper.widgets.DCBannerWidget.ACTION_REMOTE_FACTORY;

public class DCBannerWidgetProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private DCBanners banners = null;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public DCBannerWidgetProvider(Context context, int widgetId) {
        this.context = context;
        this.banners = LoadAssets.getDCBannersInstance();
        this.widgetId = widgetId;
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
    public void onDataSetChanged() {
//        Intent intent = new Intent(context, DCBannerWidget.class);
//        intent.setAction(ACTION_REMOTE_FACTORY);
//        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
//        context.sendBroadcast(intent);
    }

    @Override
    public int getCount() {
        return banners != null ? banners.getBanners().size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner_flip);
        DCBanners.Banner banner = banners.getBanner(position);

        views.setTextViewText(R.id.dcbanner_flip_label, banner.getFormattedTimeLeft());
        views.setImageViewBitmap(R.id.dcbanner_flip_image, banner.getBannerBitmap());

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