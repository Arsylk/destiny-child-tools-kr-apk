package com.arsylk.mammonsmite.Async;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.arsylk.mammonsmite.Async.interfaces.OnPackFinishedListener;
import com.arsylk.mammonsmite.DestinyChild.DCTools;

import java.io.File;
import java.lang.ref.WeakReference;

public class AsyncPack extends AsyncTask<File, Void, File> {
    private WeakReference<Context> context;
    private boolean showGui;
    private OnPackFinishedListener onPackFinishedListener = null;

    private AlertDialog dialog = null;

    public AsyncPack(Context context, boolean showGui) {
        this.context = new WeakReference<>(context);
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
            dialog = new AlertDialog.Builder(context.get())
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
                return DCTools.pack(files[0]);
            }else {
                return DCTools.pack(files[0], files[1]);
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
