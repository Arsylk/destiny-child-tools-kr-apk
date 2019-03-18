package com.arsylk.dcwallpaper.DestinyChild;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        public void loadImageBitmap(Context context, final FutureCallback<Bitmap> callback) {
            if(!imageFile.exists()) {
                Ion.with(context).load(imageUrl)
                .asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if(e == null) {
                            Utils.bitmapToFile(result, imageFile);
                            bannerBitmap = result;
                            if(callback != null) {
                                callback.onCompleted(null, bannerBitmap);
                            }
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                bannerBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if(callback != null) {
                    callback.onCompleted(null, bannerBitmap);
                }
            }
        }

        public void loadArticleDates(Context context, final FutureCallback<Boolean> callback) {
            if(!isArticleLoaded()) {
                Ion.with(context).load(articleUrl)
                        .asString(Charset.forName("KSC5601")).setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if(e == null) {
                            Document document = Jsoup.parse(result);
                            for(String articleTextLine : document.select("#tbody *").eachText()) {
                                Matcher dateMatcher = Define.PATTERN_BANNER_DATE.matcher(articleTextLine);
                                if(dateMatcher.matches()) {
                                    matchPossibleDates(dateMatcher);
                                    if(callback != null) {
                                        callback.onCompleted(null, true);
                                    }
                                    break;
                                }
                            }
                        }else {
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                if(callback != null) {
                    callback.onCompleted(null, false);
                }
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
                dateStart = String.format("%04d %02d/%02d %02d:00 +0900", Calendar.getInstance().get(Calendar.YEAR), asInts[0], asInts[1], asInts[2]);
                dateEnd = String.format("%04d %02d/%02d %02d:00 +0900", Calendar.getInstance().get(Calendar.YEAR), asInts[3], asInts[4], asInts[5]);
            }else {
                dateStart = String.format("%04d %02d/%02d 04:00 +0900",
                        Calendar.getInstance().get(Calendar.YEAR),
                        Integer.valueOf(dateMatcher.group(1)),
                        Integer.valueOf(dateMatcher.group(2)));
                dateEnd = String.format("%04d %02d/%02d 04:00 +0900",
                        Calendar.getInstance().get(Calendar.YEAR),
                        Integer.valueOf(dateMatcher.group(3)),
                        Integer.valueOf(dateMatcher.group(4)));
            }
            stringsToDate();
        }

        private void stringsToDate() {
            //date string to actual date
            try {
                SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy MM/dd HH:mm Z");
                SimpleDateFormat destFormat = new SimpleDateFormat("yyyy MM/dd HH:mm Z");

                Date parsedStart = sourceFormat.parse(dateStart);
                Date parsedEnd = sourceFormat.parse(dateEnd);
                Log.d("mTag:Start", parsedStart.toString());
                Log.d("mTag:End", parsedEnd.toString());

                Log.d("mTag:parsedStart", Utils.betweenDates(Calendar.getInstance().getTime(), parsedStart));
                Log.d("mTag:parsedEnd", Utils.betweenDates(Calendar.getInstance().getTime(), parsedEnd));
                Log.d("mTag:space", "--------------------------------------------------------------------");


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

    public Future webLoad(Context context, final FutureCallback<DCBanners> callback) {
        return Ion.with(context).load("https://cafe.naver.com/MyCafeIntro.nhn?clubid=27917479")
                .asString(Charset.forName("KSC5601")).setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if(e == null) {
                    // parse document
                    Document document = Jsoup.parse(result);
                    Elements elements = document.select("#editorMainContent a[href]");

                    // iter all banners
                    banners.clear();
                    for(int i = 0; i < elements.size(); i++) {
                        Element element = elements.get(i);
                        if(element.selectFirst("img[src]") != null) {
                            String articleUrl = element.attr("href");
                            String imageUrl = element.selectFirst("img").attr("src");
                            String articleId = articleUrl.substring(articleUrl.lastIndexOf("/")+1);
                            String imageId = element.selectFirst("img").attr("id").replaceAll("[^0-9]", "");

                            // add banner
                            banners.add(new Banner(imageUrl, articleId, imageId));
                        }
                    }

                    // save banners
                    save();
                    loadStatus = 2;
                    if(callback != null) {
                        callback.onCompleted(null, DCBanners.this);
                    }
                }else {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadAllBitmaps(Context context) {
        for(Banner banner : banners) {
            banner.loadImageBitmap(context, null);
        }
    }

    public void loadAllArticles(Context context) {
        for(Banner banner : banners) {
            banner.loadArticleDates(context, null);
        }
        save();
    }

    public void addBanner(String imageUrl, String articleId, String imageId) {
        banners.add(new Banner(imageUrl, articleId, imageId));
    }

    public void addBanner(Banner banner) {
        banners.add(banner);
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
