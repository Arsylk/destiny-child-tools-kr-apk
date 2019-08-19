package com.arsylk.mammonsmite.DestinyChild;

import android.util.Log;
import com.arsylk.mammonsmite.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arsylk.mammonsmite.DestinyChild.DCDefine.*;

public class DCLocale extends Pck {

    public DCLocale(Pck pck) {
        super(pck);
        for(PckFile pckFile : files) {
            pckFile.setExt(UNKNOWN);
        }
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

    public void patch(DCLocalePatch patch) throws Exception {
        //iter over pck files
        for(PckFile pckFile : files) {
            String hash = Utils.bytesToHex(pckFile.getHash()).toLowerCase();
            DCLocalePatch.Subfile subfile = patch.getHashFile(hash);
            if(subfile != null) {
                //pck file to map
                LinkedHashMap<String, String> defaultDict = loadFile(pckFile);
                //subfile to map
                LinkedHashMap<String, String> patchDict = subfile.getDict();

                //add & replace values found in patch
                for(Map.Entry<String,String> patchEntry : patchDict.entrySet()) {
                    defaultDict.put(patchEntry.getKey(), patchEntry.getValue());
                }

                //save file with updated map
                saveFile(pckFile, defaultDict);
            }
        }
    }

    public static LinkedHashMap<String, String> loadFile(PckFile pckFile) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Log.d("mTag:Read","Reading: "+pckFile.getFile().getName());
        try {
            List<String> lines = FileUtils.readLines(pckFile.getFile(), Charset.forName("utf-8"));
            Pattern pattern = null;
            for(String line : lines) {
                if(line.length() > 1) {
                    if(line.startsWith("/") || line.substring(1).startsWith("/")) {
                        continue;
                    }
                }
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
                    }else if(pattern == LOCALE_DEF_LINE_PATTERN){
                        //ghetto fix for broken lines
                        if(line.contains("=")) {
                            // fail safe
                            try {
                                int index = line.indexOf("=");
                                String key = line.substring(0, index).trim();
                                String val = line.substring(index+1).trim();


                                if(val.charAt(0) == '"') val = val.substring(1);
                                if(val.charAt(val.length()-1) == '"') val = val.substring(0, val.length()-1);
                                map.put(key, val);
                            }catch(Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
