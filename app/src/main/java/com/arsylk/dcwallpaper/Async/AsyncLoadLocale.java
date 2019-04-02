package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import android.util.Log;
import com.arsylk.dcwallpaper.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.dcwallpaper.DestinyChild.DCLocale;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;

import java.io.File;

public class AsyncLoadLocale extends AsyncWithDialog<File, Void, DCLocalePatch> {
    private OnLocaleUnpackFinished onLocaleUnpackFinished = null;

    public AsyncLoadLocale(Context context, boolean showGui) {
        super(context, showGui, "Unpacking locale...");
    }

    public AsyncLoadLocale setOnLocaleUnpackFinished(OnLocaleUnpackFinished onLocaleUnpackFinished) {
        this.onLocaleUnpackFinished = onLocaleUnpackFinished;
        return this;
    }

    @Override
    protected DCLocalePatch doInBackground(File... files) {
        if(files.length > 0) {
            try {
                if(!Define.ASSET_UNPACKED_LOCALE.exists() || files.length == 2) {
                    //unpack game file
                    DCLocale locale = new DCLocale(DCTools.unpack(files[0], context));
                    DCLocalePatch patch = new DCLocalePatch(locale);

                    //save to file
                    patch.save(Define.ASSET_UNPACKED_LOCALE);
                }

                //load from json file
                return new DCLocalePatch(Utils.fileToJson(Define.ASSET_UNPACKED_LOCALE));
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
