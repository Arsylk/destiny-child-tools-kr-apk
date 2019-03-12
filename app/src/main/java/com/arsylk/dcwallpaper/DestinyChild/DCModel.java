package com.arsylk.dcwallpaper.DestinyChild;

import android.util.Log;
import com.arsylk.dcwallpaper.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.*;

import static com.arsylk.dcwallpaper.DestinyChild.DCDefine.*;


public class DCModel extends Pck {
    public static class DCModelJson {
        private JSONObject json;
        private boolean tested = false, loaded = false;

        private String model;
        private String[] textures;
        private LinkedHashMap<String, String> motions, expressions;

        public DCModelJson(JSONObject json) {
            this.json = json;
            this.tested = test();
            if(isTested()) {
                this.loaded = load();
            }
        }

        private boolean test() {
            if(json != null) {
                if(json.has("model") && json.has("textures") && json.has("motions")) {
                   return true;
                }
            }
            return false;
        }

        private boolean load() {
            try {
                //character.dat
                model = json.getString("model");
                //textures
                JSONArray jsonTextures = json.getJSONArray("textures");
                textures = new String[jsonTextures.length()];
                for(int i = 0; i < textures.length; i++) {
                    textures[i] = jsonTextures.getString(i);
                }
                //motions
                JSONObject jsonMotions = json.getJSONObject("motions");
                Iterator<String> jsonMotionsKeys = jsonMotions.keys();
                List<String> listKeys = new ArrayList<>();
                while(jsonMotionsKeys.hasNext()) {
                    listKeys.add(jsonMotionsKeys.next());
                }
                Collections.sort(listKeys, new Comparator<String>() {
                    @Override
                    public int compare(String t1, String t2) {
                        return t1.toLowerCase().compareTo(t2.toLowerCase());
                    }
                });
                motions = new LinkedHashMap<>();
                for(String key : listKeys) {
                    motions.put(key, jsonMotions.getJSONArray(key).getJSONObject(0).getString("file"));
                }
                //expressions
                expressions = new LinkedHashMap<>();
                if(json.has("expressions")) {
                    JSONArray jsonExpressions = json.getJSONArray("expressions");
                    for(int i = 0; i < jsonExpressions.length(); i++) {
                        JSONObject jsonExp = jsonExpressions.getJSONObject(i);
                        expressions.put(jsonExp.getString("name"), jsonExp.getString("file"));
                    }
                }
                return true;
            }catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        //getters
        public boolean isTested() {
            return tested;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public String getModel() {
            return isLoaded() ? model : null;
        }

        public String[] getTextures() {
            return isLoaded() ? textures : null;
        }

        public LinkedHashMap<String, String> getMotions() {
            return isLoaded() ? motions : null;
        }

        public LinkedHashMap<String, String> getExpressions() {
            return isLoaded() ? expressions : null;
        }
    }
    private boolean loaded = false;
    private DCModelJson modelJson;
    private PckFile modelFile;
    private PckFile[] modelTextures;
    private PckFile modelCharacter;
    private Map<String, PckFile> modelMotions;

    public DCModel(Pck pck) {
        super(pck);
        this.loaded = loadAsModel();
        generateHeader();
    }

    //methods
    private boolean loadAsModel() {
        //try get model json
        if(tryGetModelJson()) {
            //try get textures
            if(tryGetTextures()) {
                //try get character
                if(tryGetCharacter()) {
                    //try get motions
                    if(tryGetMotions()) {
                        tryGetExpressions();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryGetModelJson() {
        long maxSize = 0L;
        PckFile maxFile = null;
        for(PckFile pckFile : getFiles(HASH_MODEL_OR_TEXTURE, JSON)) {
            long testSize = pckFile.getFile().length();
            if(testSize > maxSize) {
                maxSize = testSize;
                maxFile = pckFile;
            }
        }

        DCModelJson testModelJson;
        if(maxFile != null) {
            testModelJson = new DCModelJson(Utils.fileToJson(maxFile.getFile()));
            if(testModelJson.isTested() && testModelJson.isLoaded()) {
                modelJson = testModelJson;
                maxFile.rename("model.json");
                modelFile = maxFile;
                return true;
            }
        }
        for(PckFile pckFile : getFiles(JSON)) {
            testModelJson = new DCModelJson(Utils.fileToJson(pckFile.getFile()));
            if(testModelJson.isTested() && testModelJson.isLoaded()) {
                modelJson = testModelJson;
                pckFile.rename("model.json");
                modelFile = pckFile;
                return true;
            }
        }
        return false;
    }

    private boolean tryGetTextures() {
        try {
            List<PckFile> pckTextures = getFiles(HASH_MODEL_OR_TEXTURE, PNG);
            String[] jsonTextures = modelJson.getTextures();
            if(pckTextures.size() < jsonTextures.length) {
                pckTextures = getFiles(PNG);
            }
            modelTextures = new PckFile[jsonTextures.length];
            for(int i = 0; i < jsonTextures.length; i++) {
                pckTextures.get(i).rename(jsonTextures[i]);
                modelTextures[i] = pckTextures.get(i);
            }
            return true;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean tryGetCharacter() {
        try {
            for(PckFile pckFile : getFiles(HASH_CHARACTER, DAT)) {
                pckFile.rename(modelJson.getModel());
                modelCharacter = pckFile;
                return true;
            }
            for(PckFile pckFile : getFiles(DAT)) {
                pckFile.rename(modelJson.getModel());
                modelCharacter = pckFile;
                return true;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean tryGetMotions() {
        //try blind get mtn
        try {
            modelMotions = new HashMap<>();
            Iterator<Map.Entry<String, String>> mtnJson = modelJson.getMotions().entrySet().iterator();
            for(PckFile mtnFile : getFiles(MTN)) {
                if(mtnJson.hasNext()) {
                    Map.Entry<String, String> entry = mtnJson.next();
                    mtnFile.rename(entry.getValue());
                    modelMotions.put(entry.getKey(), mtnFile);
                }
            }
            return true;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean tryGetExpressions() {
        //blind try get exp
        try {
            if(modelJson.getExpressions() != null) {
                Iterator<Map.Entry<String, String>> expJson = modelJson.getExpressions().entrySet().iterator();
                for(PckFile expFile : getFiles(JSON)) {
                    if(!expFile.getFile().getName().equals("model.json") && expJson.hasNext()) {
                        Map.Entry<String, String> entry = expJson.next();
                        expFile.rename(entry.getValue());
                    }
                }
                //TODO usage of expressions
            }
            return true;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public static void generateModel(String modelPath, String modelId, String name) {
        try{
            File output = new File(modelPath);
            if(output.isFile()) {
                output = output.getParentFile();
            }
            FileUtils.write(new File(output, "_model"),
                    new JSONObject().put("model_id", modelId).put("model_name", name)
                            .toString(4), Charset.forName("utf-8"));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //getters
    public boolean isLoaded() {
        return loaded;
    }

    public PckFile getModelFile() {
        return modelFile;
    }
}
