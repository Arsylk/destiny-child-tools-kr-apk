package com.arsylk.mammonsmite.Adapters;

import com.arsylk.mammonsmite.DestinyChild.DCModelInfo;
import com.arsylk.mammonsmite.DestinyChild.DCWiki;

import java.io.File;

public class DCModelItem {
    private File file = null;
    private String model_id = null, model_flag = null;
    private String name = null, title = null;
    private int wikiElement = 0, wikiType= 0;
    private boolean loaded = false, wikiLoaded = false;

    public DCModelItem(File file) {
        load(file);
    }

    //methods
    private void load(File file) {
        this.file = file;
        if(file.exists() && file.canRead()) {
            String[] parts = file.getName().split("_");
            if(parts.length >= 2) {
                model_id = parts[0];
                model_flag = parts[1].replace(".pck", "");
                name = DCModelInfo.getInstance().getModelName(model_id);
                title = DCModelInfo.getInstance().getModelTitle(model_id, model_flag);
                if(title != null && name != null) {
                    loaded = true;
                    if(DCWiki.getInstance().hasChildWiki(model_id)) {
                        DCWiki.Child wikiChild =  DCWiki.getInstance().getChildWiki(model_id);
                        wikiElement = wikiChild.getElement();
                        wikiType = wikiChild.getType();
                        wikiLoaded = true;
                    }
                }
            }
        }
    }

    //getters
    public File getFile() {
        return file;
    }

    public String getModelId() {
        return model_id;
    }

    public String getModelFlag() {
        return model_flag;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormatted() {
        return (loaded) ? (getTitle() + " " + getName()).trim() : getFile().getName().replace(".pck", "");
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getWikiElement() {
        return wikiElement;
    }

    public int getWikiType() {
        return wikiType;
    }

    public boolean isWikiLoaded() {
        return wikiLoaded;
    }
}
