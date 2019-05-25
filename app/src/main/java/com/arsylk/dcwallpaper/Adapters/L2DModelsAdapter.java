package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.arsylk.dcwallpaper.R;

import java.util.List;

public class L2DModelsAdapter extends BaseAdapter {
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
        imglabel.setImageBitmap(item.getPreview());

        return convertView;
    }
}
