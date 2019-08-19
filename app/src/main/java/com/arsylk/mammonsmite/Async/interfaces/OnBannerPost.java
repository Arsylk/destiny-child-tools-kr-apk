package com.arsylk.mammonsmite.Async.interfaces;

import com.arsylk.mammonsmite.DestinyChild.DCBanners;

public interface OnBannerPost {
    void onProgressUpdate(DCBanners.Banner... banners);
}
