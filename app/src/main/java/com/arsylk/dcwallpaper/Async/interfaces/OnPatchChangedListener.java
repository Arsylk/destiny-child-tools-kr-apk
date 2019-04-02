package com.arsylk.dcwallpaper.Async.interfaces;

import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;

public interface OnPatchChangedListener {
    void onPatchChanged(DCLocalePatch patch, DCLocalePatch.Subfile subfile, String key, String val);
}
