package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import com.arsylk.dcwallpaper.utils.Define;
import com.koushikdutta.ion.Ion;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;

public class DCAnnouncementItem {
    private String id, title, url, author, date, views, thumb;
    private File thumbFile;
    private Bitmap thumbBitmap = null;
    private String translated = null;
    private boolean showTranslated = false;

    public DCAnnouncementItem(String id, String title, String url, String author, String date, String views, String thumb) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.author = author;
        this.date = date;
        this.views = views;
        this.thumb = thumb;
        this.thumbFile = new File(Define.BITMAP_CACHE_DIRECTORY, id+"_announcement.png");
    }

    public boolean loadBitmap(Context context) {
        boolean wasCached = true;
        if(!thumbFile.exists()) {
            wasCached = false;
            try {
                Ion.with(context).load(thumb).write(thumbFile).get();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        thumbBitmap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());

        return wasCached;
    }

    public boolean loadTranslated() {
        boolean wasTranslated = false;
        try {
            String rawTranslated = Jsoup.connect(String.format(Define.REMOTE_TRANSLATE_TEXT, id, TextUtils.htmlEncode(title)))
                    .ignoreContentType(true).get().body().text();
            JSONObject jsonTranslated = new JSONObject(rawTranslated);
            if(jsonTranslated.getBoolean("successful")) {
                wasTranslated = true;
                translated = jsonTranslated.getString("translated");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return wasTranslated;
    }

    public void setShowTranslated(boolean showTranslated) {
        this.showTranslated = showTranslated;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTranslatedTitle() {
        return translated != null ? translated : title;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getViews() {
        return views;
    }

    public String getThumb() {
        return thumb;
    }

    public Bitmap getThumbBitmap() {
        return thumbBitmap;
    }

    public boolean isShowTranslated() {
        return showTranslated;
    }
}
