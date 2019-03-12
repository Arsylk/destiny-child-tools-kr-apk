package com.arsylk.dcwallpaper.DestinyChild;

import android.content.Context;
import android.util.Log;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arsylk.dcwallpaper.DestinyChild.DCDefine.*;

public class DCLocale extends Pck {
    public static class Patch {
        private JSONObject patchJson = null;
        private String name = null, date = null;
        private Map<String, LinkedHashMap<String, String>> hashFiles;

        public Patch(JSONObject json) {
            load(json);
        }

        public Patch(DCLocale locale) {
            load(locale);
        }

        public Patch(Map<String, LinkedHashMap<String, String>> hashFiles) {
            this.hashFiles = hashFiles;
        }

        private void load(JSONObject json) {
            patchJson = json;
            hashFiles = new HashMap<>();
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
                            JSONObject jsonPatchDict = jsonPatchFiles.getJSONObject(patchFileKey).getJSONObject("dict");
                            Iterator<String> dictKeys = jsonPatchDict.keys();
                            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
                            while(dictKeys.hasNext()) {
                                String dictKey = dictKeys.next();
                                linkedHashMap.put(dictKey, jsonPatchDict.getString(dictKey));
                            }
                            hashFiles.put(patchFileKey, linkedHashMap);
                        }

                    }
                }

            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        private void load(DCLocale locale) {
            hashFiles = new HashMap<>();
            for(PckFile pckFile : locale.getFiles()) {
                String hash = Utils.bytesToHex(pckFile.getHash());
                LinkedHashMap<String, String> pckFileMap = locale.loadFile(pckFile);
                hashFiles.put(hash, pckFileMap);
            }
        }

        public void save(File file) {
            try {
                JSONObject generated = new JSONObject();
                JSONObject files = new JSONObject();
                for(Map.Entry<String, LinkedHashMap<String, String>> hashMapEntry : hashFiles.entrySet()) {
                    JSONObject dict = new JSONObject();
                    for(Map.Entry<String, String> entry : hashMapEntry.getValue().entrySet()) {
                        dict.put(entry.getKey(), entry.getValue());
                    }
                    files.put(hashMapEntry.getKey(), dict);
                }
                generated.put("files", files);
                if(name != null)
                    generated.put("name", name);
                if(date != null)
                    generated.put("date", date);
                FileUtils.write(file, generated.toString(4), Charset.forName("utf-8"));
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        public Map<String, LinkedHashMap<String, String>> getHashFiles() {
            return hashFiles;
        }

        public LinkedHashMap<String, String> getHashFile(String hash) {
            return hashFiles.containsKey(hash) ? hashFiles.get(hash) : null;
        }

        public JSONObject getJson() {
            return patchJson;
        }
    }

    public DCLocale(Pck pck) {
        super(pck);
        for(PckFile pckFile : files) {
            pckFile.setExt(UNKNOWN);
        }
    }

    public LinkedHashMap<String, String> loadFile(PckFile pckFile) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Log.d("mTag:Read","Reading: "+pckFile.getFile().getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(pckFile.getFile()));
            String line;
            Pattern pattern = null;
            while((line = br.readLine()) != null) {
                if(pattern == null) {
                    if(line.contains("=")) {
                        if(LOCALE_DEF_LINE_PATTERN.matcher(line).matches()) {
                            pckFile.setExt(LOCALE_DEF);
                            pattern = LOCALE_DEF_LINE_PATTERN;
                        }
                    }
                    if(pattern == null) {
                        if(LOCALE_TAB_LINE_PATTERN.matcher(line).matches()) {
                            pckFile.setExt(LOCALE_TAB);
                            pattern = LOCALE_TAB_LINE_PATTERN;
                        }
                    }
                }
                if(pattern != null) {
                    Matcher matcher = pattern.matcher(line);
                    if(matcher.matches()) {
                        map.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public void saveFile(PckFile pckFile, Map<String, String> map) throws Exception {
        //only handle files with known ext
        if((pckFile.getExt() == LOCALE_DEF || pckFile.getExt() == LOCALE_TAB)) {
            FileOutputStream out = new FileOutputStream(pckFile.getFile());
            //utf-8-bom prefix
            out.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
            out.write(new byte[]{(byte)0x0D, (byte)0x0A});
            for(String key : map.keySet()) {
                //write map to file
                if(pckFile.getExt() == LOCALE_DEF) {
                    out.write(String.format(("%s = \"%s\""), key, map.get(key)).getBytes(Charset.forName("utf-8")));
                }else if(pckFile.getExt() == LOCALE_TAB) {
                    out.write(key.getBytes(Charset.forName("utf-8")));
                    out.write(new byte[]{(byte)0x09});
                    out.write(map.get(key).getBytes(Charset.forName("utf-8")));
                }
                //write new line
                out.write(new byte[]{(byte)0x0D, (byte)0x0A});
            }
            out.close();
        }
    }

    public void patch(Context context) throws Exception {
        //get patch
        Patch patch = LoadAssets.getDCEnglishPatch();
        JSONObject jsonPatch = patch.getJson();

        //TODO rework
        //fileLoad full json
        if(jsonPatch == null || !jsonPatch.has("files"))
            return;
        //get files array
        JSONObject patch_files = jsonPatch.getJSONObject("files");
        for(PckFile pckFile : files) {
            String hash = Utils.bytesToHex(pckFile.getHash()).toLowerCase();
            //only fileLoad if hash in json
            if(patch_files.has(hash)) {
                //get file dict
                JSONObject patch_file = patch_files.getJSONObject(hash);
                if(patch_file.has("dict")) {
                    //fileLoad file to map
                    Map<String, String> map = loadFile(pckFile);
                    //iter over dict
                    JSONObject patch_file_dict = patch_file.getJSONObject("dict");
                    Iterator<String> keys = patch_file_dict.keys();
                    while(keys.hasNext()) {
                        //update map values
                        String key = keys.next(), value = patch_file_dict.getString(key);
                        map.put(key, value);
                    }
                    //save file with updated map
                    saveFile(pckFile, map);
                }
            }
        }
    }

}
