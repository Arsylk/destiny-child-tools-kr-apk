package com.arsylk.mammonsmite.Live2D;

import android.content.Context;

import net.rbgrn.android.glwallpaperservice.*;

public class L2DWallpaperRenderer extends L2DRenderer implements GLWallpaperService.Renderer {
    public L2DWallpaperRenderer(Context context) {
        super(new L2DConfig(context, L2DConfig.MODE_WALLPAPER));
    }
}
