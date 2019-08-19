package com.arsylk.mammonsmite.Async;

import android.content.Context;
import android.os.Process;
import com.arsylk.mammonsmite.DestinyChild.DCModelInfo;
import com.arsylk.mammonsmite.DestinyChild.DCWiki;
import com.arsylk.mammonsmite.utils.LoadAssets;

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
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);
        boolean error = false;
//        try {
//            publishProgress("Updating event banners...");
//            LoadAssets.updateBannerEvents(context).get();
//        }catch(Exception e) {
//            e.printStackTrace();
//            error = true;
//        }
        try {
            publishProgress("Updating child skills...");
            LoadAssets.updateChildSkills(context.get()).get();
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }
        try {
            publishProgress("Updating equipment stats...");
            LoadAssets.updateEquipmentStats(context.get()).get();
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }
        try {
            publishProgress("Updating soul cartas...");
            LoadAssets.updateSoulCartas(context.get()).get();
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }
        try {
            publishProgress("Updating child names ...");
            LoadAssets.updateChildNames(context.get());
        }catch(Exception e) {
            e.printStackTrace();
            error = true;
        }

        publishProgress("Loading wiki pages...");
        DCWiki.getInstance(true);

        publishProgress("Loading child names...");
        DCModelInfo.getInstance(true);

        return !error;
    }
}