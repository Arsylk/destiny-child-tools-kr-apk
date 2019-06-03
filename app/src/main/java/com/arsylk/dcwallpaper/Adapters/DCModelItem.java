package com.arsylk.dcwallpaper.Adapters;

import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.utils.LoadAssets;

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
                name = LoadAssets.getDCModelInfoInstance().getModelName(model_id);
                title = LoadAssets.getDCModelInfoInstance().getModelTitle(model_id, model_flag);
                if(title != null && name != null) {
                    loaded = true;
                    if(LoadAssets.getDCWikiInstance().hasWikiPage(model_id)) {
                        DCWiki.Page wikiPage =  LoadAssets.getDCWikiInstance().getWikiPage(model_id);
                        wikiElement = wikiPage.getElement();
                        wikiType = wikiPage.getType();
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
        return (loaded) ? String.valueOf((getTitle() + " " + getName()).trim()) : getFile().getName().replace(".pck", "");
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
