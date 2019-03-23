package com.arsylk.dcwallpaper.Async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;

import java.io.File;

public class AsyncUnpack extends AsyncTask<File, Void, DCModel> {
    private Context context;
    private boolean showGui = false;
    private OnUnpackFinishedListener onUnpackFinishedListener = null;

    private ProgressDialog dialog = null;

    public AsyncUnpack(Context context, boolean showGui) {
        this.context = context;
        this.showGui = showGui;
    }

    public AsyncUnpack setOnUnpackFinishedListener(OnUnpackFinishedListener onUnpackFinishedListener) {
        this.onUnpackFinishedListener = onUnpackFinishedListener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(showGui) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setTitle("Unpacking file...");
            dialog.show();
        }
    }

    @Override
    protected DCModel doInBackground(File... files) {
        if(files.length < 1)
            return null;
        try {
            return new DCModel(DCTools.unpack(files[0], context));
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(DCModel dcModel) {
        if(showGui) {
            dialog.dismiss();
        }
        if(onUnpackFinishedListener != null) {
            onUnpackFinishedListener.onFinished(dcModel);
        }
    }
}
