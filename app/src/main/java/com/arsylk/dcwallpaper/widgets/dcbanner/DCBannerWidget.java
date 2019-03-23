package com.arsylk.dcwallpaper.widgets.dcbanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Utils;

import java.util.Calendar;

public class DCBannerWidget extends AppWidgetProvider {
    public static final String ACTION_DATA_SET_CHANGED = "ACTION_DATA_SET_CHANGED";
    public static final String ACTION_DATA_VALUES_CHANGED = "ACTION_DATA_VALUES_CHANGED";
    public static final String ACTION_BANNER_CLICK = "ACTION_BANNER_CLICK";


    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dcbanner);
        Intent intent = new Intent(context, DCBannerWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.dcbanner_view_flipper, intent);

        Intent openIntent = new Intent(context, DCBannerWidget.class);
        openIntent.setAction(ACTION_BANNER_CLICK);
        openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        openIntent.setData(Uri.parse(openIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingOpenIntent = PendingIntent.getBroadcast(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.dcbanner_view_flipper, pendingOpenIntent);

        startTimerTask(context);
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    private static PendingIntent startTimerIntent = null;
    public static void startTimerTask(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(startTimerIntent != null) {
            alarm.cancel(startTimerIntent);
        }
        startTimerIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, DCBannerWidget.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, DCBannerWidget.class)))
                        .setAction(ACTION_DATA_VALUES_CHANGED), PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.set(Calendar.MILLISECOND, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MINUTE, 1);
        time.add(Calendar.HOUR, 0);
        alarm.setRepeating(AlarmManager.RTC, time.getTime().getTime(), 60*1000, startTimerIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        Log.d("mTag:Receive", "On receive: "+intent.getAction());
        //apply new data set
        if(intent.getAction().equals(ACTION_DATA_SET_CHANGED)) {
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.dcbanner_view_flipper);
            updateAppWidget(context, appWidgetManager, widgetId);
        }
        //apply new values
        if(intent.getAction().equals(ACTION_DATA_VALUES_CHANGED)) {
            int widgetIds[] = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if(widgetIds != null)
                for(int widgetIdd : widgetIds)
                    appWidgetManager.notifyAppWidgetViewDataChanged(widgetIdd, R.id.dcbanner_view_flipper);
        }
        //open article url
        if(intent.getAction().equals(ACTION_BANNER_CLICK)) {
            try {
                PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("URL"))), 0).send();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] widgetIds) {
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

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if(startTimerIntent != null)
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(startTimerIntent);
    }
}

