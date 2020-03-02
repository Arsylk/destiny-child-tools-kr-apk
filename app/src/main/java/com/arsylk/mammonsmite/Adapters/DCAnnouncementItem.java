package com.arsylk.mammonsmite.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import com.koushikdutta.ion.Ion;

import java.io.File;

public class DCAnnouncementItem {
    private String url, banner;
    private File bannerFile;
    private Bitmap bannerBitmap = null;

    public DCAnnouncementItem(String url, String banner) {
        this.url = url;
        this.banner = banner.replace("https", "http");
        this.bannerFile = new File(Define.BITMAP_CACHE_DIRECTORY, Utils.md5(banner)+"_announcement.png");
    }

    public boolean loadBitmap(Context context) {
        boolean wasCached = true;
        if(!bannerFile.exists()) {
            wasCached = false;
            try {
                Ion.with(context).load(banner).write(bannerFile).get();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        bannerBitmap = BitmapFactory.decodeFile(bannerFile.getAbsolutePath());

        return wasCached;
    }

    public String getUrl() {
        return url;
    }

    public String getBanner() {
        return banner;
    }

    public Bitmap getBannerBitmap() {
        return bannerBitmap;
    }
}
