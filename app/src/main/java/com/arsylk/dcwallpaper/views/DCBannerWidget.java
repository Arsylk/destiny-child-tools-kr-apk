package com.arsylk.dcwallpaper.views;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import com.arsylk.dcwallpaper.DestinyChild.DCBanners;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;

import java.util.*;

public class DCBannerWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager, final int widgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner);
        if(Utils.getWidgetPref(context, widgetId,"enabled_banners") == null) {
            views.setImageViewResource(R.id.dcbanner_image, R.drawable.banner_destinychild);
        }else {
            final String[] enabledBanners = Utils.getWidgetPref(context, widgetId, "enabled_banners").split(",");
            final Integer[] pointer = new Integer[] {0};
            if(enabledBanners.length > 0) {
                final Handler handler = new Handler();
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        final DCBanners.Banner banner = LoadAssets.getDCBannersInstance().getBanner(Integer.valueOf(enabledBanners[pointer[0]]));
                        if(banner != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    views.setImageViewBitmap(R.id.dcbanner_image, banner.getBannerBitmap());
                                    views.setTextViewText(R.id.dcbanner_label, banner.getFormattedTimeLeft());
                                    appWidgetManager.updateAppWidget(widgetId, views);
                                }
                            });

                        }
                        pointer[0]++;
                        if(pointer[0] >= enabledBanners.length) {
                            pointer[0] = 0;
                        }
                    }
                }, 0, 4000);
            }
        }
        appWidgetManager.updateAppWidget(widgetId, views);
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

