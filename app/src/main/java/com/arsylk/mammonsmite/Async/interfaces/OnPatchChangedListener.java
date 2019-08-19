package com.arsylk.mammonsmite.Async.interfaces;

import com.arsylk.mammonsmite.DestinyChild.DCLocalePatch;

public interface OnPatchChangedListener {
    void onPatchChanged(DCLocalePatch patch, DCLocalePatch.Subfile subfile, String key, String val);
}
