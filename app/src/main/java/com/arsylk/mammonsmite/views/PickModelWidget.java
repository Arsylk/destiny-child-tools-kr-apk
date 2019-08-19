package com.arsylk.mammonsmite.views;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import com.arsylk.mammonsmite.Live2D.LiveWallpaperService;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;


public class PickModelWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        //get remote view
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_pick_model);
        views.setOnClickPendingIntent(R.id.appwidget_text,
                PendingIntent.getBroadcast(context, 0,
                new Intent(context, PickModelWidget.class).setAction("RANDOM_MODEL"), 0)
        );
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //proof of concept change model with widget
        if(intent.getAction().equals("RANDOM_MODEL")) {
            new AlertDialog.Builder(context).setTitle("Test").setPositiveButton("Ok", null).create().show();
            int tries = 0;
            File[] unpackDirs = Utils.getUnpackPath().listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            while(tries < unpackDirs.length*2) {
                File testDir = unpackDirs[new Random().nextInt(unpackDirs.length)];
                File testModel = new File(testDir, "model.json");
                if(testModel.exists()) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putString("wallpaper_model", testModel.getAbsolutePath())
                            .putFloat("scale", 1.7f)
                            .putFloat("offset_x", 0.0f)
                            .putFloat("offset_y", 0.0f)
                            .apply();
                    Log.e("mTag:Widg", "My widget "+intent.getAction());

                    if(LiveWallpaperService.getInstance() != null)
                        LiveWallpaperService.getInstance().requestReload();
                    break;
                }
                tries+=1;
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for(int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

