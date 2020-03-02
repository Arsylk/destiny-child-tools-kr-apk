package com.arsylk.mammonsmite.Async;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import com.arsylk.mammonsmite.Adapters.DCAnnouncementItem;
import com.arsylk.mammonsmite.Async.interfaces.OnAnnouncementPost;
import com.arsylk.mammonsmite.utils.Define;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.Arrays;
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
        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
        List<DCAnnouncementItem> announcementList = new ArrayList<>();
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLSv1");
            sslcontext.init(null, null, null);
            SSLSocketFactory sslSocketFactory = new Tls12SocketFactory(sslcontext.getSocketFactory());

            Document document = Jsoup.connect(Define.ONLINE_ANNOUNCEMNT_BANNERS)
                    .sslSocketFactory(sslSocketFactory)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();
            Elements elements = document.select("a");
            publishProgress();
            for(Element element : elements) {
                String url = null, banner = null;

                String href = element.attr("href");
                if(href.startsWith("http")) {
                    url = href;
                    banner = element.selectFirst("img[src]").attr("src");
                }else {
                    String parts[] = element.attr("onclick").split(",");
                    if(parts.length >= 2) {
                        url = parts[0].substring(parts[0].indexOf("'")+1);
                        url = url.substring(0, url.indexOf("'"));

                        banner = parts[1].substring(parts[1].indexOf("'")+1);
                        banner = banner.substring(0, banner.indexOf("'"));
                        banner = banner.replace("https://", "http://");
                    }
                }

                DCAnnouncementItem announcement = new DCAnnouncementItem(url, banner);
                if(announcement.getBanner() != null) {
                    announcement.loadBitmap(context.get());
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
