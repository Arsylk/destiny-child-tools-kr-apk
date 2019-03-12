package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class L2DModelsAdapter extends BaseAdapter {
    public static class L2DModelItem {
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
            System.out.println(_preview+" "+_preview.exists());
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
    private Context context;
    private List<L2DModelItem> src_models, models;

    public L2DModelsAdapter(Context context, List<L2DModelItem> models) {
        this.context = context;
        this.src_models = models;
        this.models = models;
    }

    public void removeItem(int i) {
        models.remove(i);
        notifyDataSetChanged();
    }

    public void removeItem(L2DModelItem item) {
        models.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public L2DModelItem getItem(int i) {
        if(models.size() > i)
            return models.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.item_l2d_model, parent, false);
        L2DModelItem item = getItem(pos);

        TextView label = convertView.findViewById(R.id.label);
        TextView sublabel = convertView.findViewById(R.id.sub_label);
        ImageView imglabel = convertView.findViewById(R.id.img_label);

        label.setText(item.getModelName());
        sublabel.setText(item.getModelId());
        if(item.getPreview() != null) {
            imglabel.setImageBitmap(item.getPreview());
        }

        return convertView;
    }
}
