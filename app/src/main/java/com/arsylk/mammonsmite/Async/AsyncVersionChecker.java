package com.arsylk.mammonsmite.Async;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.arsylk.mammonsmite.BuildConfig;
import com.arsylk.mammonsmite.utils.Define;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.ion.Ion;

import java.lang.ref.WeakReference;


public class AsyncVersionChecker extends AsyncTask<Void, Void, AsyncVersionChecker.Version> {
    private WeakReference<Context> context;

    public AsyncVersionChecker(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Version doInBackground(Void... voids) {
        try {
            // check version from server
            return Ion.with(context.get())
                    .load(Define.REMOTE_CHECK_VERSION)
                    .as(new TypeToken<Version>() {}).get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Version version) {
        // check retrieved version
        if(version != null) {
            if(version.code > BuildConfig.VERSION_CODE) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                builder.setTitle(String.format("New Update! v%s -> v%s", BuildConfig.VERSION_NAME, version.name));
                builder.setMessage(version.notes);
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.get().startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
                    }
                });
                builder.create().show();
            }
        }
    }

    protected static class Version {
        public int code;
        public String name;
        public String notes;
    }
}
