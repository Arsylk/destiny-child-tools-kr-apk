package com.arsylk.dcwallpaper.DestinyChild;

import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONObject;

public class DCModelInfo {
    private JSONObject namesJson, infoJson;

    public DCModelInfo() {
        load();
    }

    private void load() {
        //load child names
        JSONObject json = Utils.fileToJson(Define.ASSET_EXTRACTED_CHILD_NAMES);
        if(json != null) {
            this.namesJson = json;
        }else {
            this.namesJson = null;
        }

        //load model info
        json = Utils.fileToJson(DCTools.getDCModelInfoPath());
        if(json != null) {
            this.infoJson = json;
        }else {
            this.infoJson = null;
        }
    }

    //getters
    public JSONObject getNamesJson() {
        return namesJson;
    }

    public JSONObject getInfoJson() {
        return infoJson;
    }

    public String getModelName(String model_id) {
        try{
            JSONObject modelJson = namesJson.getJSONObject(model_id);
            return modelJson.getString("name");
        }catch(Exception e) {
            return null;
        }
    }

    public String getModelTitle(String model_id, String model_flag) {
        try{
            JSONObject modelJson = namesJson.getJSONObject(model_id);
            JSONObject modelVariants = modelJson.getJSONObject("variants");
            JSONObject modelFlag = modelVariants.getJSONObject(model_flag);
            return modelFlag.getString("title");
        }catch(Exception e) {
            return null;
        }
    }

    public String getModelFull(String modelIdx) {
        try {
            String[] parts = modelIdx.split("_");
            return getModelTitle(parts[0], parts[1]) + " " + getModelName(parts[0]);
        }catch(Exception e) {
            return null;
        }
    }

    public JSONObject getModelInfo(String modelIdx) {
        try{
            return infoJson.getJSONObject(modelIdx);
        }catch(Exception e) {
            return null;
        }
    }
}
