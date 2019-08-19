package com.arsylk.mammonsmite.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.arsylk.mammonsmite.DestinyChild.DCBanners;
import com.arsylk.mammonsmite.R;

public class ViewFactory {

    public static ImageView getBannerView(Context context, final DCBanners.Banner banner) {
        final ImageView imageView = new ImageView(context);
        imageView.setTag(banner);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (banner.isBitmapLoaded()) {
            imageView.setImageBitmap(banner.getBannerBitmap());
        } else {
            imageView.setImageResource(R.drawable.ic_error_outline_black);
        }

        return imageView;
    }
}
