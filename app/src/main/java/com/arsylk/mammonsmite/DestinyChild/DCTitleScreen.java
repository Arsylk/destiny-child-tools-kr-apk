package com.arsylk.mammonsmite.DestinyChild;
import com.arsylk.mammonsmite.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.arsylk.mammonsmite.utils.Define.*;


public class DCTitleScreen {
    private File file;
    private boolean valid = false;

    int view_idx_len = -1, stage_len = -1;
    int view_idx_pos = -1, stage_pos = -1, x_pos = -1, y_pos = -1, x_scale_pos = -1, y_scale_pos = -1;
    String view_idx = "", stage = "";
    float x = -1, y = -1, x_scale = -1, y_scale = -1;


    public DCTitleScreen(File file) {
        this.file = file;
        try {
            this.valid = load();
        }catch(Exception ignored) {
        }
    }

    private boolean load() throws Exception {
        //buffer title screen bytes
        RandomAccessFile fs = new RandomAccessFile(file, "r");
        MappedByteBuffer mbb = fs.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fs.length()).load();
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.position(0);

        //view_idx position
        view_idx_pos = Utils.findBytePatternPosition(mbb, BYTE_PATTERN_ASSET_VIEW_IDX) + BYTE_PATTERN_ASSET_VIEW_IDX.length;
        if(view_idx_pos != BYTE_PATTERN_ASSET_VIEW_IDX.length - 1) {
            for(int i = 0; mbb.get(view_idx_pos + i) != 0x5C; i++) {
                view_idx = String.format("%s%c", view_idx, mbb.get(view_idx_pos + i));
            }
            view_idx_len = view_idx.length();
        }

        //stage position
        stage_pos = Utils.findBytePatternPosition(mbb, BYTE_PATTERN_ASSET_STAGE);
        if(stage_pos != -1) {
            for(int i = 0; mbb.get(stage_pos + i) != 0x00; i++) {
                stage = String.format("%s%c", stage, mbb.get(stage_pos + i));
            }
            stage_len = stage.length();
        }

        //offset position
        int offset_pos = Utils.findBytePatternPosition(mbb, BYTE_PATTERN_ASSET_OFFSETS) + BYTE_PATTERN_ASSET_OFFSETS.length;
        if(offset_pos != BYTE_PATTERN_ASSET_OFFSETS.length - 1) {
            x_pos = offset_pos + 19; x = mbb.getFloat(x_pos);
            y_pos = offset_pos + 23; y = mbb.getFloat(y_pos);
            x_scale_pos = offset_pos + 31; x_scale = mbb.getFloat(x_scale_pos);
            y_scale_pos = offset_pos + 35; y_scale = mbb.getFloat(y_scale_pos);
        }


        return !view_idx.isEmpty() && view_idx_pos != -1;
    }

    public void save() throws Exception {
        //buffer title screen bytes
        RandomAccessFile fs = new RandomAccessFile(file, "r");
        MappedByteBuffer mbb = fs.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fs.length()).load();
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.position(0);

        //write to new file
        File temp = new File(file.getParent(), "_temp");
        FileOutputStream fos = new FileOutputStream(temp);
        while(mbb.position() < mbb.limit()) {
            //view_idx
            if(mbb.position() == view_idx_pos) {
                fos.write(view_idx.getBytes());
                mbb.position(mbb.position() + view_idx_len);
                continue;
            }

            //stage
            if(mbb.position() == stage_pos) {
                fos.write(stage.getBytes());
                mbb.position(mbb.position() + stage_len);
                continue;
            }

            //position
            if(mbb.position() == x_pos) {
                fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(x).array());
                mbb.position(mbb.position() + 4);
                continue;
            }
            if(mbb.position() == y_pos) {
                fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(y).array());
                mbb.position(mbb.position() + 4);
                continue;
            }
            if(mbb.position() == x_scale_pos) {
                fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(x_scale).array());
                mbb.position(mbb.position() + 4);
                continue;
            }
            if(mbb.position() == y_scale_pos) {
                fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(y_scale).array());
                mbb.position(mbb.position() + 4);
                continue;
            }

            fos.write(mbb.get());
        }
        fos.flush();
        fos.close();

        //backup & move temp
        File bak = new File(file.getParent(), file.getName() + ".bak");
        if(!bak.exists()) {
            FileUtils.moveFile(file, bak);
        }else {
            FileUtils.deleteQuietly(file);
        }
        FileUtils.moveFile(temp, file);
    }

    //getters
    public File getFile() {
        return file;
    }

    public boolean isValid() {
        return valid;
    }

    public String getViewIdx() {
        return view_idx;
    }

    public String getStage() {
        return stage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getXScale() {
        return x_scale;
    }

    public float getYScale() {
        return y_scale;
    }

    //setters
    public void setViewIdx(String view_idx) {
        this.view_idx = view_idx;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setXScale(float x_scale) {
        this.x_scale = x_scale;
    }

    public void setYScale(float y_scale) {
        this.y_scale = y_scale;
    }
}
