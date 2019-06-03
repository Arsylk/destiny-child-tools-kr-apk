package com.arsylk.dcwallpaper.views;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;

public class TabSpannableFactory extends Spannable.Factory {
    @Override
    public Spannable newSpannable(CharSequence source) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String[] parts = source.toString().split("\t");
        for(int i = 0; i < parts.length-1; i++) {
            ssb.append(parts[i]);
            ssb.append("\\t");
            ssb.setSpan(new BackgroundColorSpan(0xFF212121), ssb.length()-2, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ssb.append(parts[parts.length-1]);

        return ssb;
    }
}
