package com.arsylk.dcwallpaper.DestinyChild;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Async.*;
import com.arsylk.dcwallpaper.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.dcwallpaper.Async.interfaces.OnPackFinishedListener;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

import static com.arsylk.dcwallpaper.utils.Define.ASSET_ENGLISH_PATCH;
import static com.arsylk.dcwallpaper.utils.Utils.bytesToHex;
import static com.arsylk.dcwallpaper.utils.Utils.getUnpackPath;
import static com.arsylk.dcwallpaper.DestinyChild.DCDefine.*;


public class DCTools {
    //paths
    public static File getDCFilesPath() {
        return new File(Environment.getExternalStorageDirectory().toString()+
                "/Android/data/"+DCPACKAGE+"/files/");
    }

    public static File getDCModelsPath() {
        return new File(Environment.getExternalStorageDirectory().toString()+
                "/Android/data/"+DCPACKAGE+"/files/asset/character/");
    }

    public static File getDCModelInfoPath() {
        return new File(getDCModelsPath(), "model_info.json");
    }

    public static String getDCLocalePath() {
        return Environment.getExternalStorageDirectory().toString()+
                "/Android/data/"+DCPACKAGE+"/files/locale.pck";
    }

    public static File getDCSoundsPath() {
        return new File(Environment.getExternalStorageDirectory().toString()+
                "/Android/data/"+DCPACKAGE+"/files/asset/sound/voice/");
    }

    public static File getDCBackgroundsPath() {
        return new File(Environment.getExternalStorageDirectory().toString()+
                "/Android/data/"+DCPACKAGE+"/files/asset/scenario/image/");
    }

    public static File getRandomDCBackground() {
        File[] bgs = DCTools.getDCBackgroundsPath().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("bg") && name.endsWith("_f.png");
            }
        });
        return bgs == null ? null : bgs[new Random().nextInt(bgs.length)];
    }

    public static String getExtStr(int extId) {
        switch(extId) {
            //json files
            case JSON:
                return "json";
            //motion files
            case MTN:
                return "mtn";
            //model files
            case DAT:
                return "dat";
            //textures
            case PNG:
                return "png";
        }
        return "unk";
    }

    public static int getExtId(int ext) {
        switch(ext) {
            //dat files
            case 109:
                return DAT;
            //mtn files
            case 35:
                return MTN;
            //png files
            case 137:
                return PNG;
            //json files
            case 123:
                return JSON;
        }
        return UNKNOWN;
    }


    //packing pck files
    public static void asyncPack(File src, File dst, Context context, OnPackFinishedListener onPackFinishedListener) {
        new AsyncPack(context, true)
                .setOnPackFinishedListener(onPackFinishedListener)
                .execute(src, dst);
    }

    public static File pack(File src, Context context) throws Exception {
        return pack(src, new File(src.getParent(), src.getParentFile().getName()+".pck"), context);
    }

    public static File pack(File src, File dst, Context context) throws Exception {
        if(src.exists()) {
            if(src.isDirectory()) {
                src = new File(src, "_header");
            }
            if(dst == null) {
                dst =  new File(src.getParent(), src.getParentFile().getName()+".pck");
            }

            //prepare
            JSONObject json = Utils.fileToJson(src);
            RandomAccessFile input = new RandomAccessFile(dst, "rw");

            //calculate initial offset
            int offset = 8 + 4 + json.length() * (8 + 1 + 4 + 4 + 8);
            //begin byte input
            input.write(PCK_IDENTIFIER);
            input.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(json.length()).array());
            for(int i = 0; i < json.length(); i++) {
                JSONObject sJson = json.getJSONObject(String.valueOf(i));
                String hash = sJson.getString("hash"), file = sJson.getString("file");
                input.write(Utils.hexToBytes(hash));
                input.write(ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put((byte)0x00).array());
                input.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(offset).array());
                long file_size = new File(src.getParent(), file).length();
                input.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)file_size).array());
                input.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(file_size).array());

                offset += file_size;
            }

            InputStream is;
            byte[] buffer = new byte[4096];
            for(int i = 0; i < json.length(); i++) {
                JSONObject sJson = json.getJSONObject(String.valueOf(i));
                is = new FileInputStream(new File(src.getParent(), sJson.getString("file")));
                int count;
                while((count = is.read(buffer)) > 0) {
                    input.write(buffer, 0, count);
                }
                is.close();
            }
            input.close();
            return dst;
        }else {
            return null;
        }
    }


    //unpacking pck files
    public static void asyncUnpack(File src, Context context, OnUnpackFinishedListener onUnpackFinishedListener) {
        new AsyncUnpack(context, true)
                .setOnUnpackFinishedListener(onUnpackFinishedListener)
                .execute(src);
    }

    public static Pck unpack(File src, Context context) throws Exception {
        return unpack(src, context, null);
    }

    public static Pck unpack(File src, Context context, FutureCallback<String> progressCallback) throws Exception {
        //create folder name
        String output = src.getName().replace(".pck", "");

        //clear output path
        if(getUnpackPath(output).isDirectory()) {
            for(File tempFile : getUnpackPath(output).listFiles()) {
                if(!tempFile.equals(src)) {
                    tempFile.delete();
                }
            }
        }

        //create new pck struct
        Pck pck = new Pck(src, getUnpackPath(output));

        //buffer src bytes
        RandomAccessFile fs = new RandomAccessFile(src, "r");
        MappedByteBuffer mbb = fs.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fs.length()).load();
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.position(0);

        //begin byte analysis
        byte[] idenftifier = new byte[8];
        //byte(8) pck identifier
        mbb.get(idenftifier);
        if(Arrays.equals(idenftifier, PCK_IDENTIFIER)) {
            //byte(4) count
            int count = mbb.getInt();
            Log.d("mTag:Unpack", "File Count: "+count);
            for(int i = 0; i < count; i++) {
                byte[] hash = new byte[8];
                int flag, offset, size, size_p, ext;
                //byte(8) hash
                mbb.get(hash);
                String hashs = Utils.bytesToHex(hash);
                //byte(1) flag
                flag = mbb.get();
                //byte(4) offset
                offset = mbb.getInt();
                //byte(4) compressed size
                size_p = mbb.getInt();
                //byte(4) original size
                size = mbb.getInt();
                //byte(4) ???
                mbb.position(mbb.position()+4);

                //save old position
                int start = mbb.position();

                //start extract file
                mbb.position(offset);
                byte[] file_bytes = new byte[size_p];
                mbb.get(file_bytes);

                //change if necessary
                if(flag == 2 || flag == 3) {
                    //aes
                    byte[] after_aes = Utils.aes_decrypt(file_bytes);
                    file_bytes = after_aes;
                }
                if(flag == 1 || flag == 3) {
                    //yappy
                    byte[] after_yappy = Utils.yappy_uncompress(file_bytes, size);
                    file_bytes = after_yappy;
                }

                ext = getExtId(file_bytes[0] & 0xFF);

                //display progress
                String logLine = String.format("File %2d/%d %s [%016X | %6d] %02d %s", i+1, count, hashs, offset, size, flag, getExtStr(ext));
                Log.d("mTag:File", logLine);
                if(progressCallback != null) progressCallback.onCompleted(null, logLine);

                //save extracted file (files starting with _ are unprocessed)
                File filepath = getUnpackPath(output, String.format("%08d.%s", i, getExtStr(ext)));
                FileOutputStream fos = new FileOutputStream(filepath);
                fos.write(file_bytes);
                fos.close();

                //add to unpacked pck object
                pck.addFile(filepath, hash, ext, i);

                //restore old position
                mbb.position(start);
            }
            pck.generateHeader();
            Log.d("mTag:Unpack", "Unpacking finished: "+pck.getOutput());
        }else {
            Log.d("mTag:Unpack", "Inccorect file!");
            return null;
        }
        mbb.clear();
        fs.close();

        return pck;
    }


    //pck files to models
    public static DCModel pckToModel(Pck pck) throws Exception {
        //create new model
        DCModel dcModel = new DCModel(pck);
        pck.generateHeader();

        return dcModel;
    }


    //file updating actions
    public static void asyncApplyModelInfo(L2DModel l2DModel, Context context, final boolean restore) {
        new AsyncWithDialog<L2DModel, String, L2DModel>(context, true, "Applying model info...") {
            @Override
            protected L2DModel doInBackground(L2DModel... l2DModels) {
                L2DModel l2DModel = l2DModels[0];
                try {
                    if(!restore) {
                        //apply new info
                        if(l2DModel.getModelInfoJson().length() > 0) {
                            JSONObject modelInfoBak = DCTools.applyModelInfo(l2DModel.getModelId(), l2DModel.getModelInfoJson());
                            l2DModel.setModelInfoBakJson(modelInfoBak);
                            l2DModel.generateModel();
                        }
                    }else {
                        //restore bak info
                        if(l2DModel.getModelInfoBakJson().length() > 0) {
                            DCTools.applyModelInfo(l2DModel.getModelId(), l2DModel.getModelInfoBakJson());
                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }

                return l2DModel;
            }
        }.execute(l2DModel);
    }

    public static JSONObject applyModelInfo(String modelIdx, JSONObject mode_info_values) throws Exception {
        JSONObject model_info = Utils.fileToJson(getDCModelInfoPath());
        JSONObject model_info_original = model_info.getJSONObject(modelIdx);

        //change value
        model_info.put(modelIdx, mode_info_values);

        //backup old file
        File bakFile = new File(getDCModelInfoPath().getAbsolutePath()+".bak");
        if(!bakFile.exists()) {
            FileUtils.moveFile(getDCModelInfoPath(), bakFile);
        }

        //write new json
        FileUtils.write(getDCModelInfoPath(), model_info.toString(2), Charset.forName("utf-8"));

        return model_info_original;
    }

    public static void patchLocale(File file_locale, DCLocalePatch patch, Context context) throws Exception {
        //load and patch locale
        DCLocale locale = new DCLocale(DCTools.unpack(file_locale, context));
        locale.patch(patch);

        //move files
        File file_backup = new File(file_locale.getParentFile(), "locale_"+Utils.getDateLabel(true)+".pck.bak");
        if(!file_backup.exists()) {
            FileUtils.moveFile(file_locale, file_backup);
        }else {
            FileUtils.forceDelete(file_locale);
        }

        //move patched file
        File packed_locale = DCTools.pack(locale.getOutput(), file_locale, context);

        //write new md5's
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("locale_md5", Utils.md5(file_locale))
                .putBoolean("update_child_names", true)
                .commit();
        Log.d("mTag:Patch", "Patched locale!");
    }


    //locale extractor
    public static void asyncExtractLocale(File src, Context context, OnLocaleUnpackFinished onFinished) {
        new AsyncLoadLocale(context, true)
                .setOnLocaleUnpackFinished(onFinished)
                .execute(src);
    }

    public static void asyncExtractChildNames(File src_locale_file, Context context) {
        new AsyncWithDialog<File, Void, Boolean>(context, true, "Extracting locale...") {
            @Override
            protected Boolean doInBackground(File... files) {
                if(files.length > 0) {
                    try {
                        DCTools.extractChildNames(files[0], context);
                        return true;
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        }.execute(src_locale_file);
    }

    public static void extractChildNames(File src, Context context) throws Exception {
        DCLocale srcLocale = new DCLocale(unpack(src, context));
        //dirty match hash
        Pck.PckFile pckModelIds = null;
        for(Pck.PckFile pckFile : srcLocale.getFiles()) {
            if(bytesToHex(pckFile.getHash()).equalsIgnoreCase("C40E0023A077CB28")) {
                pckModelIds = pckFile;
                break;
            }
        }
        if(pckModelIds != null) {
            //get model id's to name and title
            JSONObject jsonWikiBase = new JSONObject();

            //iter lines
            LinkedHashMap<String, String> mapModelIds = srcLocale.loadFile(pckModelIds);
            for(Map.Entry<String, String> entry : mapModelIds.entrySet()) {
                //try/catch for safety
                try {
                    String key = entry.getKey(); String value = entry.getValue();
                    //only if matches format
                    if(key.contains("_") && value.contains("\t")) {
                        value = value.substring(0, value.indexOf("\t"));
                        String modelId = key.substring(0, key.indexOf("_")),
                                modelFlag = key.substring(key.indexOf("_")+1),
                                modelTitle = value.substring(0, value.indexOf("_")),
                                modelName = value.substring(value.indexOf("_")+1);
                        value = value.replace("_", " ").trim();
                        //adding to new json
                        if(!jsonWikiBase.has(modelId)) {
                            jsonWikiBase.put(modelId, new JSONObject());
                        }
                        JSONObject jsonModelId = jsonWikiBase.getJSONObject(modelId);
                        if(jsonModelId.has("name")) {
                            if(jsonModelId.getString("name").isEmpty()) {
                                jsonModelId.put("name", modelName);
                            }
                        }else {
                            jsonModelId.put("name", modelName);
                        }

                        if(!jsonModelId.has("variants")) {
                            jsonModelId.put("variants", new JSONObject());
                        }
                        JSONObject jsonModelVariants = jsonModelId.getJSONObject("variants");
                        jsonModelVariants.put(modelFlag, new JSONObject().put("title", modelTitle));
                    }
                }catch(Exception e) {
                    System.err.append("key: "+entry.getKey()+"  val: "+entry.getValue()+"\n");
                    e.printStackTrace();
                }
            }
            //save to file
            FileUtils.write(Define.ASSET_EXTRACTED_CHILD_NAMES, jsonWikiBase.toString(4), Charset.forName("utf-8"));
        }
    }

    public static void extractCardNames(File src, Context context) throws Exception {
        DCLocale srcLocale = new DCLocale(unpack(src, context));
        //dirty match hash
        Pck.PckFile pckCardDescs = null, pckCardSkills = null;
        for(Pck.PckFile pckFile : srcLocale.getFiles()) {
            if(bytesToHex(pckFile.getHash()).equalsIgnoreCase("8C0A00198AD12EE5")) {
                pckCardDescs = pckFile;
            }else if(bytesToHex(pckFile.getHash()).equalsIgnoreCase("9C0F002568FDB06B")) {
                pckCardSkills = pckFile;
            }
        }
        if(pckCardDescs != null && pckCardSkills != null) {
            LinkedHashMap<String, String> cardDescsMap = srcLocale.loadFile(pckCardDescs);
            LinkedHashMap<String, String> cardSkillsMap = srcLocale.loadFile(pckCardSkills);
            List<String> keyList = new ArrayList<>();
            for(String key : cardDescsMap.keySet()) {
                if(key.startsWith("51")) {
                    if(key.startsWith("515") && key.endsWith("6")) continue;
                    keyList.add(key);
                }
            }
            for(String key : keyList) {
                String newKey = "1"+key.substring(2, key.length()-1)+"50";
                Log.d("mTag:Card", key+" => "+cardDescsMap.get(key)+"\n"+newKey+" => "+cardSkillsMap.get(newKey));
            }
            Log.d("mTag:CardSize", keyList.size()+"");



            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        FileOutputStream out = new FileOutputStream(new File(Define.BASE_DIRECTORY, "idx_carta"));
                        for(File file : getUnpackPath("pack").listFiles()) {
                            try {
                                String string = FileUtils.readFileToString(file, Charset.forName("utf-8"));
                                if(string.contains("idx") && string.contains("carta")) {
                                    out.write((file.getName()+"\n").getBytes(Charset.forName("utf-8")));
                                    FileUtils.moveFile(file, new File(file.getParentFile(), "_"+file.getName()));
                                }
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        out.close();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void asyncExtractMissing(File src, Context context, boolean showGui) {
        new AsyncWithDialog<File, Void, File>(context, showGui, "Extracting new keys...") {
            @Override
            protected File doInBackground(File... files) {
                File extracted = null;
                try {
                    extracted = DCTools.extractMissing(files[0], context);
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return extracted;
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                if(file != null) {
                    Toast.makeText(context, "Saved to: "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, "Failed to extract!", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(src);
    }

    public static File extractMissing(File src, Context context) throws Exception {
        //load patch if present
        DCLocalePatch patch = new DCLocalePatch(Utils.fileToJson(ASSET_ENGLISH_PATCH));

        //load locale
        DCLocale srcLocale = new DCLocale(unpack(src, context));
        new DCLocalePatch(srcLocale).save(new File(Define.BASE_DIRECTORY, "extracted_current.json"));

        JSONObject generated = new JSONObject();
        generated.put("name", "Not in patch");
        generated.put("files", new JSONObject());
        for(Pck.PckFile pckFile : srcLocale.getFiles()) {
            try {
                String hash = Utils.bytesToHex(pckFile.getHash()).toLowerCase();
                LinkedHashMap<String, String> pckFileMap = DCLocale.loadFile(pckFile);
                DCLocalePatch.Subfile patchSubfile = patch.getHashFile(hash);
                if(patchSubfile.getDict() != null) {
                    for(String patchFileKey : new ArrayList<String>(patchSubfile.getDict().keySet())) {
                        pckFileMap.remove(patchFileKey);
                    }
                }

                JSONObject generatedDict = new JSONObject();
                for(LinkedHashMap.Entry<String, String> entry : pckFileMap.entrySet()) {
                    generatedDict.put(entry.getKey(), entry.getValue());
                }


                generated.getJSONObject("files").put(hash, new JSONObject()
                        .put("hash", hash)
                        .put("line_type", pckFile.getExt()-11)
                        .put("dict", generatedDict));
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(Define.BASE_DIRECTORY, "extracted_new.json");
        FileUtils.write(file, generated.toString(4), Charset.forName("utf-8"));

        return file;
    }


    //developer tools
    public static void fullFilesDump(Context context) {
        new AsyncWithDialog<String, Integer, Void>(context, true, "Generating md5's...") {
            @Override
            protected void onPreExecute() {
                if(showGui) {
                    dialog = new ProgressDialog(context);
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    dialog.setMessage(message);
                    dialog.show();
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if(dialog != null && values.length > 1) {
                    dialog.setIndeterminate(false);
                    dialog.setProgress(values[0]);
                    dialog.setMax(values[1]);
                }
            }

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    FileOutputStream out = new FileOutputStream(new File(Define.BASE_DIRECTORY, "md5_"+strings[0]));
                    List<File> allFiles = new ArrayList<>(FileUtils.listFiles(getDCFilesPath(), null, true));
                    Collections.sort(allFiles);
                    for(int i = 0; i < allFiles.size(); i++) {
                        File file = allFiles.get(i);
                        String formatted = file.getAbsolutePath().replace(getDCFilesPath().getAbsolutePath(), "");
                        out.write(String.format("%s %s\n", formatted, Utils.md5(file)).getBytes(Charset.forName("utf-8")));
                        publishProgress(i, allFiles.size());
                    }
                    out.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute("all");
    }
}