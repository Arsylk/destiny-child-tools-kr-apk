package com.arsylk.dcwallpaper.Async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class AsyncWithDialog<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Context context;
    protected boolean showGui = true;
    protected String message = "";
    protected ProgressDialog dialog = null;

    public AsyncWithDialog(Context context, boolean showGui) {
        init(context, showGui, "");
    }

    public AsyncWithDialog(Context context, boolean showGui, String message) {
        init(context, showGui, message);

    }

    private void init(Context context, boolean showGui, String message) {
        this.context = context;
        this.showGui = showGui;
        this.message = message;
    }

    @Override
    protected void onPreExecute() {
        if(showGui) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setMessage(message);
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if(showGui) {
            dialog.dismiss();
        }
    }
}
