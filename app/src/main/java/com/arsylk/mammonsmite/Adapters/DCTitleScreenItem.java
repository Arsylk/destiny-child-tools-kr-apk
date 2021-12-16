package com.arsylk.mammonsmite.Adapters;

import com.arsylk.mammonsmite.DestinyChild.DCModelInfo;

import java.io.File;
import java.util.regex.Matcher;

import static com.arsylk.mammonsmite.utils.Define.PATTERN_TITLE_SCREEN;

public class DCTitleScreenItem {
    private File file;
    private String model_id = null, model_flag = null;
    private String name = null, title = null;
    private boolean loaded = false;


    public DCTitleScreenItem(File file) {
        load(file);
    }

    //methods
    private void load(File file) {
        this.file = file;
        if(file.exists() && file.canRead()) {

            Matcher matcher = PATTERN_TITLE_SCREEN.matcher(file.getName().toLowerCase());
            if(matcher.matches()) {
                model_id = matcher.group(1);
                model_flag = matcher.group(2) != null ? matcher.group(2) : "02";
                name = DCModelInfo.getInstance().getModelName(model_id);
                title = DCModelInfo.getInstance().getModelTitle(model_id, model_flag);

                loaded = name != null && title != null;
            }
        }
    }

    //getters
    public File getFile() {
        return file;
    }

    public String getFormatted() {
        return loaded ? (title + " " + name).trim() : file.getName();
    }
}
