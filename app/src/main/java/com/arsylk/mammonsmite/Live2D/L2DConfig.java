package com.arsylk.mammonsmite.Live2D;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.utils.Define;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;

public class L2DConfig {
    public static final int MODE_PEEK = -1, MODE_PREVIEW = 0, MODE_WALLPAPER = 1, BG_MODE_SCALE_XY = 0, BG_MODE_FIT_X = 1, BG_MODE_FIT_Y = 2;

    //defaults
    private int mode = MODE_PREVIEW, bgMode = BG_MODE_SCALE_XY;
    private String modelPath = null, backgroundPath = null;
    private float modelScale = 1.0f, modelOffsetX = 0.0f, modelOffsetY = 0.0f;
    private boolean animated = true, tappable = true, sounds = false;
    private boolean requestReload = false;

    //constructors
    public L2DConfig(Context context, int mode) {
        if(context instanceof Activity) {
            context = ((Activity) context).getBaseContext();
        }else if(context instanceof Application) {
            context = ((Application) context).getBaseContext();
        }
        this.mode = mode;
        readFromPrefs(context);
    }

    public L2DConfig(Context context, int mode, String modelPath) {
        this.mode = mode;
        this.modelPath = modelPath;
        if(new File(modelPath).exists()) {
            readFromModel();
        }else {
            readFromPrefs(context);
        }

    }

    //methods
    public void readFromPrefs(Context context) {
        String prefix = getMode() == MODE_PREVIEW ? "preview_" : "wallpaper_";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        backgroundPath = prefs.getString(prefix+"bg", null);
        if(backgroundPath == null) {
            File file = DCTools.getRandomDCBackground();
            backgroundPath = file == null ?
                    backgroundPath = new File(Define.UNPACKER_DIRECTORY, "bg.png").getAbsolutePath() :
                    file.getAbsolutePath();
        }
        bgMode = prefs.getInt(prefix+"bg_mode", BG_MODE_SCALE_XY);
        modelPath = prefs.getString(prefix+"model", "");
        modelScale = prefs.getFloat(prefix+"scale", 1.0f);
        modelOffsetX = prefs.getFloat(prefix+"offset_x", 0.0f);
        modelOffsetY = prefs.getFloat(prefix+"offset_y", 0.0f);
        animated = prefs.getBoolean(prefix+"animated", true);
        tappable = prefs.getBoolean(prefix+"tappable", true);
        sounds = prefs.getBoolean(prefix+"sounds", false);
        
    }
    
    public void writeToPrefs(Context context) {
        String prefix = getMode() == MODE_PREVIEW ? "preview_" : "wallpaper_";
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(prefix+"bg", getBackgroundPath())
                .putInt(prefix+"bg_mode", getBgMode())
                .putString(prefix+"model", getModelPath())
                .putFloat(prefix+"scale", getModelScale())
                .putFloat(prefix+"offset_x", getModelOffsetX())
                .putFloat(prefix+"offset_y", getModelOffsetY())
                .putBoolean(prefix+"animated", isAnimated())
                .putBoolean(prefix+"tappable", isTappable())
                .putBoolean(prefix+"sounds", isTappable())
                .apply();
    }

    public void readFromModel() {
        try {
            L2DModel l2DModel = new L2DModel(getModelPath());
            JSONObject _modelJson = l2DModel.getModelConfigJson();
            if(_modelJson.has("bg"))
                this.backgroundPath = _modelJson.getString("bg");
            if(_modelJson.has("bg_mode"))
                this.bgMode = _modelJson.getInt("bg_mode");
            if(_modelJson.has("model"))
                this.modelPath = _modelJson.getString("model");
            if(_modelJson.has("scale"))
                this.modelScale = (float) _modelJson.getDouble("scale");
            if(_modelJson.has("offset_x"))
                this.modelOffsetX = (float) _modelJson.getDouble("offset_x");
            if(_modelJson.has("offset_y"))
                this.modelOffsetY = (float) _modelJson.getDouble("offset_y");
            if(_modelJson.has("animated"))
                this.animated = _modelJson.getBoolean("animated");
            if(_modelJson.has("tappable"))
                this.tappable = _modelJson.getBoolean("tappable");
            if(_modelJson.has("sounds"))
                this.sounds = _modelJson.getBoolean("sounds");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToModel() {
        try {
            L2DModel l2DModel = new L2DModel(getModelPath());
            JSONObject _modelJson = l2DModel.getModelConfigJson()
                    .put("bg", getBackgroundPath())
                    .put("bg_mode", getBgMode())
                    .put("scale", getModelScale())
                    .put("offset_x", getModelOffsetX())
                    .put("offset_y", getModelOffsetY())
                    .put("animated", isAnimated())
                    .put("tappable", isTappable())
                    .put("sounds", isSounds());
            FileUtils.write(l2DModel.getModelConfig(), _modelJson.toString(4), Charset.forName("utf-8"));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    //setters & getters
    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getBgMode() {
        return bgMode;
    }

    public void setBgMode(int bgMode) {
        this.bgMode = bgMode;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String dcModelPath) {
        this.modelPath = dcModelPath;
    }

    public String getBackgroundPath() {
        if(backgroundPath == null) {
            File file = DCTools.getRandomDCBackground();
            if(file != null) {
                backgroundPath = file.getAbsolutePath();
            }else {
                backgroundPath = new File(Define.UNPACKER_DIRECTORY, "bg.png").getAbsolutePath();
            }

        }

        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public float getModelScale() {
        return modelScale;
    }

    public void setModelScale(float modelScale) {
        this.modelScale = modelScale;
    }

    public float getModelOffsetX() {
        return modelOffsetX;
    }

    public void setModelOffsetX(float modelOffsetX) {
        this.modelOffsetX = modelOffsetX;
    }

    public float getModelOffsetY() {
        return modelOffsetY;
    }

    public void setModelOffsetY(float modelOffsetY) {
        this.modelOffsetY = modelOffsetY;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean isTappable() {
        return tappable;
    }

    public void setTappable(boolean tappable) {
        this.tappable = tappable;
    }

    public boolean isSounds() {
        return sounds;
    }

    public void setSounds(boolean sounds) {
        this.sounds = sounds;
    }

    public boolean shouldReload() {
        if(requestReload) {
            requestReload = false;
            return true;
        }
        return false;
    }

    public void requestReload() {
        this.requestReload = true;
    }
}
