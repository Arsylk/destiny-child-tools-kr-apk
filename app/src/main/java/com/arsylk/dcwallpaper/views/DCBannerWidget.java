package com.arsylk.dcwallpaper.views;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;

public class DCBannerWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager, final int widgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner);
        if(Utils.getWidgetPref(context, widgetId,"enabled_banners") != null) {
            final String[] enabledBanners = Utils.getWidgetPref(context, widgetId, "enabled_banners").split(",");
            for(String enabledBanner : enabledBanners) {
                DCBanners.Banner banner = LoadAssets.getDCBannersInstance().getBanner(Integer.valueOf(enabledBanner));
                RemoteViews flipView = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner_flip);
                flipView.setImageViewBitmap(R.id.dcbanner_flip_image, banner.getBannerBitmap());
                flipView.setTextViewText(R.id.dcbanner_flip_label, banner.getFormattedTimeLeft());

                views.addView(R.id.dcbanner_view_flipper, flipView);
            }

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] widgetIds) {
        LoadAssets.getDCBannersInstance().webLoad(context, null);
        for(int widgetId : widgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            Utils.removeWidgetPref(context, appWidgetId);
        }
    }
}

