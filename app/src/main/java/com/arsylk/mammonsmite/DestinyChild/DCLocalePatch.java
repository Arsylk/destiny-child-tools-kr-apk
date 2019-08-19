package com.arsylk.mammonsmite.DestinyChild;

import com.arsylk.mammonsmite.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class DCLocalePatch {
    public static class Subfile {
        private String hash;
        private int lineType;
        private LinkedHashMap<String, String> dict;

        //constructors
        public Subfile(String hash, int lineType, LinkedHashMap<String, String> dict) {
            this.hash = hash;
            this.lineType = lineType;
            this.dict = dict;
        }

        public Subfile(Pck.PckFile pckFile) {
            this.dict = DCLocale.loadFile(pckFile);
            this.hash = Utils.bytesToHex(pckFile.getHash()).toLowerCase();
            this.lineType = pckFile.getExt()-11;
        }

        public Subfile(JSONObject jsonFile) {
            try {
                this.dict = new LinkedHashMap<>();
                this.hash = jsonFile.getString("hash").toLowerCase();
                this.lineType = jsonFile.getInt("line_type");
                JSONObject jsonDict = jsonFile.getJSONObject("dict");
                Iterator<String> keys = jsonDict.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    this.dict.put(key, jsonDict.getString(key));
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        //methods
        public void setValue(String key, String value) {
            dict.put(key, value);
        }

        public void delValue(String key) {
            if(dict.containsKey(key)) {
                dict.remove(key);
            }
        }

        //null safe getters
        public String getValue(String key) {
            return dict.containsKey(key) ? dict.get(key) : null;
        }

        public String getHash() {
            return hash;
        }

        public int getLineType() {
            return lineType;
        }

        public LinkedHashMap<String, String> getDict() {
            return dict;
        }

        public boolean queryDictKey(String queryKey) {
            for(String key : dict.keySet()) {
                if(key.toLowerCase().contains(queryKey.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        public boolean queryDictVal(String queryVal) {
            for(String val : dict.values()) {
                if(val.toLowerCase().contains(queryVal.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }
    private JSONObject patchJson = null;
    private String name = null, date = null;
    private LinkedHashMap<String, Subfile> hashFiles;

    //constructors
    public DCLocalePatch(JSONObject json) {
        patchJson = json;
        hashFiles = new LinkedHashMap<>();
        try {
            if(json.has("name"))
                name = json.getString("name");
            if(json.has("date"))
                date = json.getString("date");
            if(json.has("files")) {
                JSONObject jsonPatchFiles = json.getJSONObject("files");
                Iterator<String> patchFileKeys = jsonPatchFiles.keys();
                while(patchFileKeys.hasNext()) {
                    String patchFileKey = patchFileKeys.next();
                    if(jsonPatchFiles.has(patchFileKey)) {
                        hashFiles.put(patchFileKey.toLowerCase(), new Subfile(jsonPatchFiles.getJSONObject(patchFileKey)));
                    }

                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public DCLocalePatch(DCLocale locale) {
        name = "extracted_from_"+locale.getSrc().getName();
        date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        hashFiles = new LinkedHashMap<>();
        for(Pck.PckFile pckFile : locale.getFiles()) {
            hashFiles.put(Utils.bytesToHex(pckFile.getHash()).toLowerCase(), new Subfile(pckFile));
        }
    }

    public DCLocalePatch(Subfile... subfiles) {
        name = "user_created";
        date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime());
        hashFiles = new LinkedHashMap<>();
        for(Subfile subfile : subfiles) {
            hashFiles.put(subfile.getHash().toLowerCase(), subfile);
        }
    }

    //methods
    public DCLocalePatch patch(DCLocalePatch patch) {
        for(Subfile subfile : hashFiles.values()) {
            Subfile patchSubfile = patch.getHashFile(subfile.getHash());
            if(patchSubfile != null) {
                for(Map.Entry<String, String> entry : subfile.getDict().entrySet()) {
                    if(patchSubfile.getDict().containsKey(entry.getKey())) {
                        subfile.setValue(entry.getKey(), patchSubfile.getValue(entry.getKey()));
                    }
                }
            }
        }

        return this;
    }

    public String generate() {
        try {
            JSONObject generated = new JSONObject();
            JSONObject files = new JSONObject();
            for(Map.Entry<String, Subfile> hashMapEntry : hashFiles.entrySet()) {
                JSONObject dict = new JSONObject();
                for(Map.Entry<String, String> entry : hashMapEntry.getValue().getDict().entrySet()) {
                    dict.put(entry.getKey(), entry.getValue());
                }
                files.put(hashMapEntry.getKey().toLowerCase(),
                        new JSONObject()
                                .put("dict", dict)
                                .put("line_type", hashMapEntry.getValue().getLineType())
                                .put("hash", hashMapEntry.getKey().toLowerCase()));
            }
            generated.put("files", files);
            if(name != null)
                generated.put("name", name);
            if(date != null)
                generated.put("date", date);

            return generated.toString(4);
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void save(File file) {
        try {
            String generated = generate();
            if(generated != null) {
                FileUtils.write(file, generate(), Charset.forName("utf-8"));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void addSubfile(Subfile subfile) {
        hashFiles.put(subfile.getHash(), subfile);
    }

    public void delSubfile(Subfile subfile) {
        hashFiles.remove(subfile.getHash());
    }

    //null safe getters
    public Subfile getHashFile(String hash) {
        return hashFiles.containsKey(hash.toLowerCase()) ? hashFiles.get(hash.toLowerCase()) : null;
    }

    public LinkedHashMap<String, String> getHashFileDict(String hash) {
        Subfile testfile = getHashFile(hash);
        if(testfile != null) {
            return testfile.getDict();
        }

        return new LinkedHashMap<>();
    }

    public String getHashFileDictValue(String hash, String key) {
        Subfile testfile = getHashFile(hash);
        if(testfile != null) {
            return testfile.getValue(key);
        }

        return null;
    }

    //getters
    public LinkedHashMap<String, Subfile> getHashFiles() {
        return hashFiles;
    }

    public JSONObject getJson() {
        return patchJson;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    //static
    public static DCLocalePatch clone(DCLocalePatch object) {
        if(object == null) return null;

        DCLocalePatch objectClone = new DCLocalePatch();
        objectClone.name = object.name;
        objectClone.date = object.date;
        for(Subfile subfile : object.getHashFiles().values())
            objectClone.addSubfile(new Subfile(subfile.getHash(), subfile.getLineType(), new LinkedHashMap<>(subfile.getDict())));

        return objectClone;
    }
}