package com.arsylk.mammonsmite.DestinyChild;

import android.util.Log;
import com.arsylk.mammonsmite.utils.Utils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pck {
    public class PckFile {
        private File file;
        private byte[] hash;
        private int ext, index;

        public PckFile(File file, byte[] hash, int ext, int index) {
            this.file = file;
            this.hash = hash;
            this.ext = ext;
            this.index = index;
        }

        public File rename(String newName) {
            file = Utils.rename(file, newName);
            return file;
        }

        //getters & setters
        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public byte[] getHash() {
            return hash;
        }

        public void setHash(byte[] hash) {
            this.hash = hash;
        }

        public int getExt() {
            return ext;
        }

        public void setExt(int ext) {
            this.ext = ext;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    protected File src, output;
    protected List<PckFile> files;

    public Pck(File src, File output) {
        this.src = src;
        this.output = output;
        this.files = new ArrayList<>();
    }

    public Pck(Pck pck) {
        this.src = pck.getSrc();
        this.output = pck.getOutput();
        this.files = pck.getFiles();
    }

    //methods
    public synchronized void generateHeader() {
        try{
            FileOutputStream _header = new FileOutputStream(new File(output, "_header"));
            JSONObject json = new JSONObject();
            for(PckFile pckFile : getFiles()) {
                JSONObject sJson = new JSONObject();
                sJson.put("hash", Utils.bytesToHex(pckFile.getHash()));
                sJson.put("file", pckFile.getFile().getName());
                json.put(String.valueOf(pckFile.getIndex()), sJson);
            }
            _header.write(json.toString(4).getBytes());
            _header.close();
            Log.d("mJson", json.toString());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //getters & setters
    public File getSrc() {
        return src;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
        for(PckFile pckFile : files) {
            pckFile.setFile(new File(output, pckFile.getFile().getName()));
        }
    }

    public void addFile(File file, byte[] hash, int ext, int index) {
        files.add(index, new PckFile(file, hash, ext, index));
    }

    public PckFile getFile(int index) {
        for(PckFile pckFile : files) {
            if(pckFile.getIndex() == index) {
                return pckFile;
            }
        }
        return null;
    }

    public PckFile getFile(byte[] hash) {
        for(PckFile pckFile : files) {
            if(Arrays.equals(pckFile.getHash(), hash)) {
                return pckFile;
            }
        }
        return null;
    }

    public List<PckFile> getFiles(byte[] hash_part, int extId) {
        List<PckFile> matchingFiles = new ArrayList<>();
        for(PckFile pckFile : getFiles(hash_part)) {
            if(pckFile.getExt() == extId) {
                matchingFiles.add(pckFile);
            }
        }
        return matchingFiles;
    }

    public List<PckFile> getFiles(byte[] hash_part) {
        List<PckFile> matchingFiles = new ArrayList<>();
        byte[] hash_useful;
        for(PckFile pckFile : files) {
            hash_useful = Arrays.copyOf(pckFile.getHash(), 4);
            if(Arrays.equals(hash_part, hash_useful)) {
                matchingFiles.add(pckFile);
            }
        }
        return matchingFiles;
    }

    public List<PckFile> getFiles(int extId) {
        List<PckFile> matchingFiles = new ArrayList<>();
        for(PckFile pckFile : files) {
            if(pckFile.getExt() == extId) {
                matchingFiles.add(pckFile);
            }
        }
        return matchingFiles;
    }

    public List<PckFile> getFiles() {
        return files;
    }
}
