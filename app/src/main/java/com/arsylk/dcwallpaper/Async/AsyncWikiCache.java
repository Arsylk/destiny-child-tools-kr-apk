package com.arsylk.dcwallpaper.Async;

import android.content.Context;
import com.arsylk.dcwallpaper.Async.interfaces.OnWikiPagePost;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;

public class AsyncWikiCache extends AsyncWithDialog<DCWiki, DCWiki.Page, DCWiki> {
    private OnWikiPagePost onWikiPagePost = null;
    private DCWiki dcWiki = null;

    public AsyncWikiCache(Context context, boolean showGui) {
        super(context, showGui, "Caching wiki bitmaps...");
    }

    public AsyncWikiCache setOnWikiPagePost(OnWikiPagePost onWikiPagePost) {
        this.onWikiPagePost = onWikiPagePost;
        return this;
    }

    @Override
    protected void onProgressUpdate(DCWiki.Page... values) {
        try {
            dialog.setMessage(String.format("%s <> %d/%d",
                    values[0].getModelId(),
                    dcWiki.getWikiPages().indexOf(values[0]),
                    dcWiki.getWikiPages().size()));
        }catch(Exception e){
            e.printStackTrace();
        }
        if(onWikiPagePost != null) onWikiPagePost.onProgressUpdate(values);
    }

    @Override
    protected DCWiki doInBackground(DCWiki... dcWikis) {
        dcWiki = dcWikis[0];
        for(int i = 0; i < dcWiki.getWikiPages().size(); i++) {
            try {
                DCWiki.Page page = dcWiki.getWikiPages().get(i);
                page.loadBitmap(context);
                publishProgress(page);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        return dcWiki;
    }
}
