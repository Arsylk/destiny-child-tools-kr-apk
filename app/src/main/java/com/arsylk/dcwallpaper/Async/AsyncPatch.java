package com.arsylk.dcwallpaper.Async;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.arsylk.dcwallpaper.DestinyChild.DCDefine;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.utils.LoadAssets;

import java.io.File;

public class AsyncPatch extends AsyncTask<DCLocalePatch, Void, Boolean> {
    private Context context;
    private boolean showGui = false;

    private AlertDialog dialog = null;

    public AsyncPatch(Context context, boolean showGui) {
        this.context = context;
        this.showGui = showGui;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(showGui) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle("Patching locale...")
                    .setItems(new String[]{"Join our discord!!!"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://discord.gg/wDdq7C8")));
                        }
                    })
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    }).setCancelable(false).show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    @Override
    protected Boolean doInBackground(DCLocalePatch... patches) {
        try {
            DCTools.patchLocale(new File(DCTools.getDCLocalePath()), patches[0], context);
            return true;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(showGui) {
            dialog.setTitle(success ? "Finished" : "Failed");
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        }
    }
}
