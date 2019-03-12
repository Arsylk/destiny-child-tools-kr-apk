package com.arsylk.dcwallpaper.Adapters;

import com.google.gson.JsonObject;

public class OnlineModelItem {
    private JsonObject json;
    private String fileModel, filePreview;
    private String modTitle, modCreator, modDescription;
    private String modelRegion, modelId;
    private float modelScale, modelOffsetX, modelOffsetY;

    //constructors
    public OnlineModelItem(JsonObject json) {
        this.json = json;
        load();
    }

    private void load() {
        try {
            //files
            fileModel = json.get("file_model").getAsString();
            filePreview = json.get("file_preview").getAsString();

            //mod
            modTitle = json.get("mod_title").getAsString();
            modCreator = json.get("mod_creator").getAsString();
            modDescription = json.get("mod_description").getAsString();

            //model
            modelRegion = "";
            int modelRegionFlags = json.get("model_region").getAsInt();
            if((modelRegionFlags & 1) != 0)
                modelRegion += "\uD83C\uDDF0\uD83C\uDDF7 ";
            if((modelRegionFlags & 2) != 0)
                modelRegion += "\uD83C\uDDEF\uD83C\uDDF5 ";
            modelRegion = modelRegion.trim();
            modelId = json.get("model_id").getAsString();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //getters
    public String getFileModel() {
        return fileModel;
    }

    public String getFilePreview() {
        return filePreview;
    }

    public String getModTitle() {
        return modTitle;
    }

    public String getModCreator() {
        return modCreator;
    }

    public String getModDescription() {
        return modDescription;
    }

    public String getModelRegion() {
        return modelRegion;
    }

    public String getModelId() {
        return modelId;
    }

    public float getModelScale() {
        return modelScale;
    }

    public float getModelOffsetX() {
        return modelOffsetX;
    }

    public float getModelOffsetY() {
        return modelOffsetY;
    }
}
