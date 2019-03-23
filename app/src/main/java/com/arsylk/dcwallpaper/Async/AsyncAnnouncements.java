package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import com.arsylk.dcwallpaper.Adapters.DCAnnouncementItem;
import com.arsylk.dcwallpaper.Async.interfaces.OnAnnouncementPost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AsyncAnnouncements extends AsyncWithDialog<Integer, DCAnnouncementItem, List<DCAnnouncementItem>> {
    private OnAnnouncementPost onAnnouncementPost;

    public AsyncAnnouncements(Context context, boolean showGui) {
        super(context, showGui);
    }

    public AsyncAnnouncements setOnAnnouncementsPost(OnAnnouncementPost onAnnouncementPost) {
        this.onAnnouncementPost = onAnnouncementPost;
        return this;
    }


    @Override
    protected void onProgressUpdate(DCAnnouncementItem... values) {
        if(onAnnouncementPost != null) onAnnouncementPost.onProgressUpdate(values);
    }

    @Override
    protected List<DCAnnouncementItem> doInBackground(Integer... integers) {
        List<DCAnnouncementItem> announcementList = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://m.cafe.naver.com/NoticeList.nhn?search.clubid=27917479").get();
            Elements elements = document.select("#articleListArea > ul.list_area li.board_box");
            publishProgress(null);
            for(Element element : elements) {
                String id, title, url, author, date, views, thumb = null;
                id = element.selectFirst("a[href]").attr("data-article-id");
                title = element.selectFirst("strong.tit").text().trim();
                url = "https://m.cafe.naver.com" + element.selectFirst("a[href]").attr("href");
                author = element.selectFirst("span.nick").text();
                date = element.selectFirst("span.time").text();
                if(date.charAt(date.length() - 1) == '.')
                    date = date.substring(0, date.length() - 2);
                views = element.selectFirst("span.no").text();
                if(views.contains("만"))
                    views = views.replace("만", "0,000");
                if(element.selectFirst("div.thumb img") != null) {
                    thumb = element.selectFirst("div.thumb img").attr("src");
                }

                DCAnnouncementItem announcement = new DCAnnouncementItem(id, title, url, author, date, views, thumb);
                announcement.loadTranslated();
                if(announcement.getThumb() != null) {
                    announcement.loadBitmap(context);
                }

                announcementList.add(announcement);
                publishProgress(announcement);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return announcementList;
    }
}
