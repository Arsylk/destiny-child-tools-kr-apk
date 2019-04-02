package com.arsylk.dcwallpaper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.arsylk.dcwallpaper.Async.AsyncLoadAssets;
import com.arsylk.dcwallpaper.DestinyChild.*;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

import static com.arsylk.dcwallpaper.utils.Define.*;

public class LoadAssets  {
    private static final int TAG_ASSETS = 732;

    public static void guiFullLoad(Context context, final Utils.Callback callback) {
        new AsyncLoadAssets(context, true) {
            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(callback != null) callback.onCall();
            }
        }.execute();
    }

    public static Future updateEnglishPatch(Context context) {
        return Ion.with(context).load(String.format(REMOTE_ASSET_LOCALE, Utils.md5(ASSET_LOCALE))).group(TAG_ASSETS)
                .asString(Charset.forName("utf-8")).setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if(e == null) {
                            if(result.isEmpty()) {
                                Log.d("mTag:Assets", "English patch is up-to-date!");
                                return;
                            }
                            try {
                                FileUtils.write(ASSET_LOCALE, result, Charset.forName("utf-8"));
                                Log.d("mTag:Assets", "English patch updated!");
                            }catch(Exception e1) {
                                e1.printStackTrace();
                            }
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static Future updateChildSkills(Context context) {
        return Ion.with(context).load(String.format(REMOTE_ASSET_CHILD_SKILLS, Utils.md5(ASSET_CHILD_SKILLS))).group(TAG_ASSETS)
                .asString(Charset.forName("utf-8")).setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if(e == null) {
                            if(result.isEmpty()) {
                                Log.d("mTag:Assets", "Child skills are up-to-date!");
                                return;
                            }
                            try {
                                FileUtils.write(ASSET_CHILD_SKILLS, result, Charset.forName("utf-8"));
                                Log.d("mTag:Assets", "Child skills updated!");
                            }catch(Exception e1) {
                                e1.printStackTrace();
                            }
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static void updateChildNames(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        File locale = new File(DCTools.getDCLocalePath());
        String oldLocaleMd5 = prefs.getString("locale_md5", "");
        String newLocaleMd5 = Utils.md5(locale);
        if(newLocaleMd5.equalsIgnoreCase(oldLocaleMd5) && ASSET_EXTRACTED_CHILD_NAMES.exists()) {
            Log.d("mTag:Assets", "Child names are up-to-date!");
        }else {
            try {
                DCTools.extractChildNames(locale, context);
                prefs.edit().putString("locale_md5", newLocaleMd5).commit();
                Log.d("mTag:Assets", "Child names updated!");
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean updateInProgress(Context context) {
        return Ion.getDefault(context).getPendingRequestCount(TAG_ASSETS) != 0;
    }

    //singleton instances
    //wiki
    private static DCWiki wiki = null;
    public static DCWiki getDCWikiInstance() {
        if(wiki == null) {
            wiki = new DCWiki();
        }
        return wiki;
    }

    //info
    private static DCModelInfo info = null;
    public static DCModelInfo getDCModelInfoInstance() {
        if(info == null) {
            info = new DCModelInfo();
        }
        return info;
    }

    //patch
    private static DCLocalePatch patch = null;
    public static DCLocalePatch getDCEnglishPatch() {
        if(patch ==  null) {
            patch = new DCLocalePatch(Utils.fileToJson(ASSET_LOCALE));
        }
        return patch;
    }
}
