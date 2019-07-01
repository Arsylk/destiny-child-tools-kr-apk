package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.koushikdutta.async.future.FutureCallback;

import java.io.File;


public class AsyncUnpack extends AsyncWithDialog<File, String, DCModel> {
    private OnUnpackFinishedListener onUnpackFinishedListener = null;

    public AsyncUnpack(Context context, boolean showGui) {
        super(context, showGui, "Unpacking...");
    }

    public AsyncUnpack setOnUnpackFinishedListener(OnUnpackFinishedListener onUnpackFinishedListener) {
        this.onUnpackFinishedListener = onUnpackFinishedListener;
        return this;
    }

    @Override
    protected void onProgressUpdate(String... messages) {
        if(messages.length > 0 && showGui) {
            dialog.setMessage(messages[0]);
        }
    }

    @Override
    protected DCModel doInBackground(File... files) {
        try {
            return new DCModel(DCTools.unpack(files[0], context.get(), new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    publishProgress(result);
                }
            }));
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(DCModel dcModel) {
        if(onUnpackFinishedListener != null) {
            onUnpackFinishedListener.onFinished(dcModel);
        }
        super.onPostExecute(dcModel);
    }
}
