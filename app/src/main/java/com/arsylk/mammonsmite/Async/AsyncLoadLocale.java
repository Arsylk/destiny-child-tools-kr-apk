package com.arsylk.mammonsmite.Async;

import android.content.Context;
import com.arsylk.mammonsmite.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.mammonsmite.DestinyChild.DCLocale;
import com.arsylk.mammonsmite.DestinyChild.DCLocalePatch;
import com.arsylk.mammonsmite.DestinyChild.DCTools;

import java.io.File;

public class AsyncLoadLocale extends AsyncWithDialog<File, String, DCLocalePatch> {
    private OnLocaleUnpackFinished onLocaleUnpackFinished = null;

    public AsyncLoadLocale(Context context, boolean showGui) {
        super(context, showGui, "Unpacking locale...");
    }

    public AsyncLoadLocale setOnLocaleUnpackFinished(OnLocaleUnpackFinished onLocaleUnpackFinished) {
        this.onLocaleUnpackFinished = onLocaleUnpackFinished;
        return this;
    }

    @Override
    protected void onProgressUpdate(String... messages) {
        if(messages.length > 0 && showGui) {
            dialog.setMessage(messages[0]);
        }
    }

    @Override
    protected DCLocalePatch doInBackground(File... files) {
        if(files.length > 0) {
            try {
                //unpack game file
                DCLocale locale = new DCLocale(DCTools.unpack(files[0], (e, result) -> publishProgress(result)));
                return new DCLocalePatch(locale);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(DCLocalePatch locale) {
        if(onLocaleUnpackFinished != null)
            onLocaleUnpackFinished.onFinished(locale);
        super.onPostExecute(locale);
    }
}
