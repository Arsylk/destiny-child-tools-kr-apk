package com.arsylk.dcwallpaper.Async;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.arsylk.dcwallpaper.Async.interfaces.OnPackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;

import java.io.File;

public class AsyncPack extends AsyncTask<File, Void, File> {
    private Context context;
    private boolean showGui = false;
    private OnPackFinishedListener onPackFinishedListener = null;

    private AlertDialog dialog = null;

    public AsyncPack(Context context, boolean showGui) {
        this.context = context;
        this.showGui = showGui;
    }

    public AsyncPack setOnPackFinishedListener(OnPackFinishedListener onPackFinishedListener) {
        this.onPackFinishedListener = onPackFinishedListener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(showGui) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle("Packing file...")
                    .setCancelable(false).show();
        }
    }

    @Override
    protected File doInBackground(File... files) {
        if(files.length < 1)
            return null;
        try {
            if(files.length < 2) {
                return DCTools.pack(files[0], context);
            }else {
                return DCTools.pack(files[0], files[1], context);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if(showGui) {
            dialog.dismiss();
        }
        if(onPackFinishedListener != null) {
            onPackFinishedListener.onFinished(file);
        }
    }
}
