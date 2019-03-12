package com.arsylk.dcwallpaper.DestinyChild;

import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONObject;

public class DCModelInfo {
    private JSONObject json;

    public DCModelInfo() {
        load();
    }

    private void load() {
        JSONObject json = Utils.fileToJson(Define.ASSET_EXTRACTED_CHILD_NAMES);
        if(json != null) {
            this.json = json;
        }else {
            this.json = null;
        }
    }

    /* getters */
    public JSONObject getJson() {
        return json;
    }

    public String getModelName(String model_id) {
        try{
            JSONObject modelJson = json.getJSONObject(model_id);
            return modelJson.getString("name");
        }catch(Exception e) {
            return null;
        }
    }

    public String getModelTitle(String model_id, String model_flag) {
        try{
            JSONObject modelJson = json.getJSONObject(model_id);
            JSONObject modelVariants = modelJson.getJSONObject("variants");
            JSONObject modelFlag = modelVariants.getJSONObject(model_flag);
            return modelFlag.getString("title");
        }catch(Exception e) {
            return null;
        }
    }
}
