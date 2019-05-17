package com.arsylk.dcwallpaper.Async;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.utils.LoadAssets;

import java.io.File;

public class AsyncPatch extends AsyncTask<File, Void, Void> {
    private Context context;
    private boolean showGui = false;
    private long time_start = 0L, time_now = 0L;

    private AlertDialog dialog = null;

    public AsyncPatch(Context context, boolean showGui) {
        this.context = context;
        this.showGui = showGui;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.time_start = System.currentTimeMillis();
        if(showGui) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle("Patching locale...")
                    .setItems(new String[]{"Join our discord!!!"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://discord.gg/wDdq7C8")));
                        }
                    })
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    }).setCancelable(false).show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    protected Void doInBackground(File... files) {
        if(files.length < 1)
            return null;
        try {
            DCTools.patchLocale(files[0], LoadAssets.getDCEnglishPatch(), context);
            this.time_now = System.currentTimeMillis();
            if(time_now-time_start < 5000L) {
                Thread.sleep(5000L - (time_now-time_start));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(showGui) {
            dialog.setTitle("Finished");
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
