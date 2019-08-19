package com.arsylk.mammonsmite.Async.interfaces;

import com.arsylk.mammonsmite.Adapters.DCAnnouncementItem;

public interface OnAnnouncementPost {
    void onProgressUpdate(DCAnnouncementItem... items);
}
