package com.arsylk.mammonsmite.DestinyChild;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class DCBanners {
    public static class Banner {
        private String articleUrl, imageUrl, articleId, imageId;
        private File imageFile;
        private String dateStart = null, dateEnd = null;
        private Date actualDateStart = null, actualDateEnd = null;
        private Bitmap bannerBitmap = null;

        //constructors
        public Banner(String imageUrl, String articleId, String imageId) {
            this.imageUrl = imageUrl;
            this.articleId = articleId;
            this.imageId = imageId;
            this.articleUrl = "https://cafe.naver.com/ArticleRead.nhn?clubid=27917479&articleid="+articleId;
            this.imageFile = new File(Define.BITMAP_CACHE_DIRECTORY, imageId+".png");
        }


        //methods
        public Bitmap loadImageBitmap(Context context) {
            if(!imageFile.exists()) {
                try {
                    Ion.with(context).load(imageUrl).write(imageFile).get();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            bannerBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            return bannerBitmap;
        }

        public boolean loadArticleDates(Context context) {
            if(!isArticleLoaded()) {
                try {
                    Document document = Jsoup.parse(Ion.with(context).load(articleUrl).asString(Charset.forName("KSC5601")).get());
                    for(String articleTextLine : document.select("#tbody *").eachText()) {
                        Matcher dateMatcher = Define.PATTERN_BANNER_DATE.matcher(articleTextLine);
                        if(dateMatcher.matches()) {
                            matchPossibleDates(dateMatcher);
                            return true;
                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }

                return false;
            }else {
                return true;
            }
        }

        private void matchPossibleDates(Matcher dateMatcher) {
            //different matches for dates
            Matcher dateTimeMatcher = Define.PATTERN_BANNER_DATE_TIME.matcher(dateMatcher.group(0));
            if(dateTimeMatcher.matches()) {
                Integer[] asInts = new Integer[dateTimeMatcher.groupCount()];
                for(int i = 0; i < asInts.length; i++) {
                    asInts[i] = Integer.valueOf(dateTimeMatcher.group(i+1));
                }
                dateStart = String.format(Locale.US, "%04d %02d/%02d %02d:00 +0900", Calendar.getInstance().get(Calendar.YEAR), asInts[0], asInts[1], asInts[2]);
                dateEnd = String.format(Locale.US, "%04d %02d/%02d %02d:00 +0900", Calendar.getInstance().get(Calendar.YEAR), asInts[3], asInts[4], asInts[5]);
            }else {
                dateStart = String.format(Locale.US, "%04d %02d/%02d 04:00 +0900",
                        Calendar.getInstance().get(Calendar.YEAR),
                        Integer.valueOf(dateMatcher.group(1)),
                        Integer.valueOf(dateMatcher.group(2)));
                dateEnd = String.format(Locale.US, "%04d %02d/%02d 04:00 +0900",
                        Calendar.getInstance().get(Calendar.YEAR),
                        Integer.valueOf(dateMatcher.group(3)),
                        Integer.valueOf(dateMatcher.group(4)));
            }
            stringsToDate();
        }

        private void stringsToDate() {
            //TODO check if isn't called to often
            //date string to actual date
            try {
                SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy MM/dd HH:mm Z", Locale.US);
                SimpleDateFormat destFormat = new SimpleDateFormat("yyyy MM/dd HH:mm Z", Locale.US);

                Date parsedStart = sourceFormat.parse(dateStart);
                Date parsedEnd = sourceFormat.parse(dateEnd);

                dateStart = destFormat.format(parsedStart);
                dateEnd = destFormat.format(parsedEnd);

                actualDateStart = parsedStart;
                actualDateEnd = parsedEnd;
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        public boolean compareIds(String articleId, String imageId) {
            return this.articleId.equalsIgnoreCase(articleId) && this.imageId.equalsIgnoreCase(imageId);
        }


        //setters & getters
        public void setDates(String dateStart, String dateEnd) {
            this.dateStart = dateStart;
            this.dateEnd = dateEnd;
            stringsToDate();
        }

        public boolean isBitmapLoaded() {
            return imageFile.exists() && bannerBitmap != null;
        }

        public boolean isArticleLoaded() {
            return dateStart != null && dateEnd != null;
        }

        public String getArticleUrl() {
            return articleUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getArticleId() {
            return articleId;
        }

        public String getImageId() {
            return imageId;
        }

        public File getImageFile() {
            return imageFile;
        }

        public String getDateStart() {
            return dateStart;
        }

        public String getDateEnd() {
            return dateEnd;
        }

        public Bitmap getBannerBitmap() {
            return bannerBitmap;
        }

        public String getFormattedTimeLeft() {
            return actualDateEnd != null ? Utils.betweenDates(Calendar.getInstance().getTime(), actualDateEnd) : "Couldn't parse dates!";
        }
    }
    private List<Banner> banners = null;
    private int loadStatus = 0;

    public DCBanners(File file) {
        banners = new ArrayList<>();
        if(file.exists()) {
            fileLoad(Utils.fileToJson(file));
        }
    }

    public synchronized void save() {
        try {
            JSONObject json = new JSONObject();
            JSONObject jsonBanners = new JSONObject();
            for(int i = 0; i < banners.size(); i++) {
                Banner banner = banners.get(i);
                JSONObject jsonBanner = new JSONObject();
                jsonBanner.put("image_url", banner.getImageUrl());
                jsonBanner.put("image_id", banner.getImageId());
                jsonBanner.put("article_url", banner.getArticleUrl());
                jsonBanner.put("article_id", banner.getArticleId());
                if(banner.isArticleLoaded()) {
                    jsonBanner.put("date_start", banner.getDateStart());
                    jsonBanner.put("date_end", banner.getDateEnd());
                }
                jsonBanners.put(String.valueOf(i), jsonBanner);
            }

            json.put("banners", jsonBanners);
            FileUtils.write(Define.ASSET_EVENT_BANNERS, json.toString(4), Charset.forName("utf-8"));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void fileLoad(JSONObject json) {
        if(json == null) return;
        banners = new ArrayList<>();
        try {
            String imageUrl, articleId, imageId, dateStart, dateEnd;
            JSONObject jsonBanners = json.getJSONObject("banners");
            for(int i = 0; i < jsonBanners.length(); i++) {
                String iStr = String.valueOf(i);
                if(jsonBanners.has(iStr)) {
                    JSONObject jsonBanner = jsonBanners.getJSONObject(iStr);
                    if(jsonBanner.has("image_url") && jsonBanner.has("article_id") && jsonBanner.has("image_id")) {
                        imageUrl = jsonBanner.getString("image_url");
                        articleId = jsonBanner.getString("article_id");
                        imageId = jsonBanner.getString("image_id");

                        Banner banner = new Banner(imageUrl, articleId, imageId);
                        if(jsonBanner.has("date_start") && jsonBanner.has("date_end")) {
                            dateStart = jsonBanner.getString("date_start");
                            dateEnd = jsonBanner.getString("date_end");
                            banner.setDates(dateStart, dateEnd);
                        }
                        banners.add(banner);
                    }
                }
            }
            loadStatus = 1;
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setBanner(Banner banner, int i) {
        if(banners.size() > i) {
            banners.set(i, banner);
        }else {
            banners.add(banner);
        }
    }

    public Banner getBanner(int i) {
        return banners.size() > i ? banners.get(i) : null;
    }

    public List<Banner> getBanners() {
        return banners;
    }

    public boolean isFileLoaded() {
        return loadStatus == 1;
    }

    public boolean isWebLoaded() {
        return loadStatus == 2;
    }
}
