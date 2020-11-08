package com.arsylk.mammonsmite.DestinyChild;

import android.widget.Toast;
import com.arsylk.mammonsmite.utils.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.arsylk.mammonsmite.utils.Define.*;
import static com.arsylk.mammonsmite.utils.Define.BYTE_PATTERN_ASSET_OFFSETS;

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
}
