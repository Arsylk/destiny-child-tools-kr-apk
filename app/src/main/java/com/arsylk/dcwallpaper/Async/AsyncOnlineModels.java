package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.arsylk.dcwallpaper.Adapters.OnlineModelItem;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.File;


public class AsyncOnlineModels extends AsyncWithDialog<Integer, OnlineModelItem, Boolean> {

    public AsyncOnlineModels(Context context, boolean showGui) {
        super(context, showGui, "Loading models...");
    }

    @Override
    protected Boolean doInBackground(Integer... offsets) {
        try {
            String response = Jsoup.connect(String.format(Define.ONLINE_MODELS_URL, offsets[0])).execute().body();
            JSONObject json = new JSONObject(response);
            JSONArray modelsJson = json.getJSONArray("models");

            // iter all models
            for(int i = 0; i < modelsJson.length(); i++) {
                OnlineModelItem onlineModel = new OnlineModelItem(modelsJson.getJSONObject(i));

                // load preview bitmap
                if(onlineModel.getPreviewUrl() != null) {
                    File previewCache = new File(Define.BITMAP_CACHE_DIRECTORY, onlineModel.getId()+"_online.png");

                    // check for cached file
                    if(!previewCache.exists()) {
                        try {
                            // load & trim bitmap
                            Bitmap previewBitmapRaw = Ion.with(context.get()).load(onlineModel.getPreviewUrl()).asBitmap().get();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            Bitmap previewBitmapCut = Utils.trim(previewBitmapRaw);
                            previewBitmapCut.compress(Bitmap.CompressFormat.PNG, 100, bos);

                            // save to file
                            FileUtils.writeByteArrayToFile(previewCache, bos.toByteArray());

                            // recycle bitmaps & close stream
                            previewBitmapRaw.recycle();
                            previewBitmapCut.recycle();
                            bos.close();
                        }catch(Exception e) {
                        }
                    }
                    onlineModel.setPreviewBitmap(BitmapFactory.decodeFile(previewCache.getAbsolutePath()));
                }

                // return loaded model
                publishProgress(onlineModel);
            }

            // check if any models left
            return modelsJson.length() > 0;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
