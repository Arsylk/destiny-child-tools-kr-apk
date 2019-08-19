package com.arsylk.mammonsmite.Async;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CachedImage extends AsyncTask<Void, Void, Bitmap> {
    private String url;
    private File cachedFile;
    private Bitmap imageBitmap = null;
    private Utils.OnPostExecute<CachedImage> onPostExecute = null;

    public CachedImage(String url, String identifier) {
        this.url = url;
        this.cachedFile = new File(Define.BITMAP_CACHE_DIRECTORY, (identifier+".png"));
    }

    public void asyncLoad(Utils.OnPostExecute<CachedImage> onPostExecute) {
        // ignore if running
        if(getStatus() == Status.RUNNING) {
            return;
        }

        // if already loaded
        if(getStatus() == Status.FINISHED || isLoaded()) {
            onPostExecute.onPostExecute(this);
            return;
        }

        // first time
        this.onPostExecute = onPostExecute;
        this.execute();
    }

    @Override
    protected Bitmap doInBackground(Void... aVoids) {
        try {
            if(!cachedFile.exists() || cachedFile.length() == 0) {
                try {
                    FileOutputStream fos = new FileOutputStream(cachedFile);
                    BufferedInputStream bis = Jsoup.connect(url).ignoreContentType(true).execute().bodyStream();
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return BitmapFactory.decodeFile(cachedFile.getAbsolutePath());
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        this.imageBitmap = bitmap;
        if(onPostExecute != null) onPostExecute.onPostExecute(this);
    }

    public String getUrl() {
        return url;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public boolean isCached() {
        return cachedFile.exists() && cachedFile.isFile();
    }

    public boolean isLoaded() {
        return getImageBitmap() != null && isCached();
    }
}
