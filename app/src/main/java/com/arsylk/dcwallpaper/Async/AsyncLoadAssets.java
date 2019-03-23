package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import com.arsylk.dcwallpaper.utils.LoadAssets;

public class AsyncLoadAssets extends AsyncWithDialog<Void, String, Boolean> {

    public AsyncLoadAssets(Context context, boolean showGui) {
        super(context, showGui, "Checking for updates...");
    }

    @Override
    protected void onProgressUpdate(String... messages) {
        if(dialog != null && messages != null) {
            if(messages.length > 0) {
                dialog.setMessage(messages[0]);
            }
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean error = false;
//        try {
//            publishProgress("Updating event banners...");
//            LoadAssets.updateBannerEvents(context).get();
//        }catch(Exception e) {
//            e.printStackTrace();
//            error = true;
//        }
        try {
            publishProgress("Updating english patch...");
            LoadAssets.updateEnglishPatch(context).get();
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }
        try {
            publishProgress("Updating child skills...");
            LoadAssets.updateChildSkills(context).get();
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }
        try {
            publishProgress("Updating child names ...");
            LoadAssets.updateChildNames(context);
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }

        publishProgress("Loading child skills...");
        LoadAssets.getDCWikiInstance();

        publishProgress("Loading child names...");
        LoadAssets.getDCModelInfoInstance();

        return !error;
    }
}
