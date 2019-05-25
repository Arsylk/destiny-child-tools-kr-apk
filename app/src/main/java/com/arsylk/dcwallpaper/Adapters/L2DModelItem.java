package com.arsylk.dcwallpaper.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONObject;

import java.io.File;

public class L2DModelItem {
    private File output, _model, _preview;
    private String modelId, modelName;
    private Bitmap preview = null;
    private boolean loaded = false;

    public L2DModelItem(File file) {
        if(file.isDirectory()) {
            file = new File(file, "_model");
        }
        if(file.exists()) {
            output = file.getParentFile();
            _model = file;
            load();
            loadPreview();
        }
    }

    private void load() {
        try {
            JSONObject json = Utils.fileToJson(_model);
            if(json != null) {
                if(json.has("model_id") && json.has("model_name")) {
                    modelId = json.getString("model_id");
                    modelName = json.getString("model_name");
                    loaded = true;
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void loadPreview() {
        _preview = new File(output, "_preview.png");
        if(_preview.exists()) {
            try{
                preview = BitmapFactory.decodeFile(_preview.getAbsolutePath());
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public File getFile() {
        return output;
    }
}