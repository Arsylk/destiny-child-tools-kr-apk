package com.arsylk.mammonsmite.Async;

import android.content.Context;
import com.arsylk.mammonsmite.Async.interfaces.OnBannerPost;
import com.arsylk.mammonsmite.DestinyChild.DCBanners;
import com.arsylk.mammonsmite.utils.Define;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AsyncBanners extends AsyncWithDialog<DCBanners, DCBanners.Banner, DCBanners> {
    private FutureCallback<DCBanners> callback = null;
    private OnBannerPost onBannerPost = null;

    public AsyncBanners(Context context, boolean showGui) {
        super(context, showGui, "Loading banners...");
    }

    public AsyncBanners setCallback(FutureCallback<DCBanners> callback) {
        this.callback = callback;
        return this;
    }

    public AsyncBanners setOnBannerPost(OnBannerPost onBannerPost) {
        this.onBannerPost = onBannerPost;
        return this;
    }

    @Override
    protected void onProgressUpdate(DCBanners.Banner... values) {
        if(onBannerPost != null) onBannerPost.onProgressUpdate(values);
    }

    @Override
    protected DCBanners doInBackground(DCBanners... dcBanners) {
        DCBanners banners;
        if(dcBanners.length > 0) {
            banners = dcBanners[0];
        }else {
            banners = new DCBanners(Define.ASSET_EVENT_BANNERS);
        }

        publishProgress();
        try{
            // load document
            String ghettoFix = Ion.with(context.get())
                    .load("https://cafe.naver.com/MyCafeIntro.nhn?clubid=27917479")
                    .setTimeout(5000).asString().get();
            Document document = Jsoup.parse(ghettoFix);
            Elements elements = document.select("#editorMainContent a[href]");


            // iter over banner elements
            for(int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                if(element.selectFirst("img[src]") != null) {
                    String articleUrl = element.attr("href");
                    String imageUrl = element.selectFirst("img").attr("src");
                    String articleId = articleUrl.substring(articleUrl.lastIndexOf("/")+1);
                    String imageId = element.selectFirst("img").attr("id").replaceAll("[^0-9]", "");


                    DCBanners.Banner newBanner = new DCBanners.Banner(imageUrl, articleId, imageId);
                    // check if any copy exists
                    if(banners.getBanners().size() > i) {
                        DCBanners.Banner oldBanner = banners.getBanners().get(i);
                        // check if article is loaded
                        if(oldBanner.isArticleLoaded()) {
                            // copy dates if same id's
                            if(oldBanner.compareIds(articleId, imageId)) {
                                newBanner.setDates(oldBanner.getDateStart(), oldBanner.getDateEnd());
                            }
                        }
                    }
                    if(!newBanner.isArticleLoaded()) {
                        // load article
                        newBanner.loadArticleDates(context.get());
                    }
                    // load bitmap
                    newBanner.loadImageBitmap(context.get());

                    // set new banner
                    banners.setBanner(newBanner, i);

                    // publish progress
                    publishProgress(newBanner);
                }
            }

            // save new banners
            banners.save();

        }catch(Exception e) {
            e.printStackTrace();
        }

        return banners;
    }


    @Override
    protected void onPostExecute(DCBanners dcBanners) {
        super.onPostExecute(dcBanners);
        if(callback != null) callback.onCompleted(null, dcBanners);
    }

}
