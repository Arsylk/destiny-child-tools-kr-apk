package com.arsylk.dcwallpaper.Live2D;

import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.List;
import java.util.Map;

public class L2DModel {
    private DCModel.DCModelJson modelJson;
    private File output, model, _model;
    private List<File> textures;
    private Map<String, String> motions;
    private String modelName, modelId;
    private JSONObject _modelJson = null;

    public L2DModel(String model) {
        load(new File(model));
    }

    public L2DModel(File model) {
        load(model);
    }

    //methods
    private void load(File model) {
        if(model.isDirectory())
            model = new File(model, "model.json");
        this.model = model;
        this.modelJson = new DCModel.DCModelJson(Utils.fileToJson(model));
        if(modelJson.isLoaded()) {
            this.output = model.getParentFile();
            this.motions = modelJson.getMotions();
            this._model = new File(output, "_model");
            if(_model.exists()) {
                _modelJson = Utils.fileToJson(_model);
                if(_modelJson != null) {
                    if(_modelJson.has("model_name") && _modelJson.has("model_id")) {
                        try {
                            this.modelId = _modelJson.getString("model_id");
                            this.modelName = _modelJson.getString("model_name");
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //getters
    public boolean isLoaded() {
        return modelJson.isLoaded();
    }

    public File getModelConfig() {
        return _model != null ? _model : new File(model.getParentFile(), "_model");
    }

    public JSONObject getModelConfigJson() {
        return _modelJson != null ? _modelJson : new JSONObject();
    }

    public File getModel() {
        return model;
    }

    public File getOutput() {
        return output;
    }

    public File getCharacter() {
        return new File(output, modelJson.getModel());
    }

    public File[] getTextures() {
        File[] textures = new File[modelJson.getTextures().length];
        for(int i = 0; i < modelJson.getTextures().length; i++) {
            textures[i] = new File(output, modelJson.getTextures()[i]);
        }
        return textures;
    }

    public File getMotion(String name) {
        if(motions.containsKey(name)) {
            return new File(output, motions.get(name));
        }
        return null;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }
}
