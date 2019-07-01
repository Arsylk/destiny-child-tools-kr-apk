package com.arsylk.dcwallpaper.Async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.arsylk.dcwallpaper.utils.Utils;

import java.lang.ref.WeakReference;

public abstract class AsyncWithDialog<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected WeakReference<Context> context;
    protected boolean showGui = true;
    protected String message = "";
    protected ProgressDialog dialog = null;
    protected Utils.OnProgressUpdate<Progress> onProgressUpdate = null;
    protected Utils.OnPostExecute<Result> onPostExecute = null;

    // quick static task
    public static void execute(Context context, final Utils.Callback doInBackgroundCallback, final Utils.Callback onPostExecuteCallback) {
        new AsyncWithDialog<Void, Void, Void>(context, false) {
            @Override
            protected Void doInBackground(Void... voids) {
                doInBackgroundCallback.onCall();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                onPostExecuteCallback.onCall();
            }
        }.execute();
    }

    public AsyncWithDialog(Context context, boolean showGui) {
        init(context, showGui, "");
    }

    public AsyncWithDialog(Context context, boolean showGui, String message) {
        init(context, showGui, message);
    }

    private void init(Context context, boolean showGui, String message) {
        this.context = new WeakReference<>(context);
        this.showGui = showGui;
        this.message = message;
    }

    public AsyncWithDialog<Params, Progress, Result> setOnProgressUpdate(Utils.OnProgressUpdate<Progress> onProgressUpdate) {
        this.onProgressUpdate = onProgressUpdate;
        return this;
    }

    public AsyncWithDialog<Params, Progress, Result> setOnPostExecute(Utils.OnPostExecute<Result> onPostExecute) {
        this.onPostExecute = onPostExecute;
        return this;
    }

    @Override
    protected void onPreExecute() {
        if(showGui) {
            dialog = new ProgressDialog(context.get());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setMessage(message);
            dialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        if(onProgressUpdate != null && values != null) {
            for(Progress value : values) {
                onProgressUpdate.onProgressUpdate(value);
            }
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if(showGui) {
            dialog.dismiss();
        }
        if(onPostExecute != null) {
            onPostExecute.onPostExecute(result);
        }
    }
}
