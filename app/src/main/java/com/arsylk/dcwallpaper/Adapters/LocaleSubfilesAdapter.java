package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.R;

import java.util.ArrayList;
import java.util.List;

public class LocaleSubfilesAdapter extends BaseAdapter {
    private Context context;
    private List<DCLocalePatch.Subfile> subfiles;
    private DCLocalePatch locale = null, patch = null;
    private int pickedPosition = -1;

    public LocaleSubfilesAdapter(Context context) {
        this.context = context;
        this.subfiles = new ArrayList<>();
    }

    public void setLocale(DCLocalePatch locale) {
        this.locale = locale;
        this.subfiles = new ArrayList<>(locale.getHashFiles().values());

        notifyDataSetChanged();
    }

    public void applyPatch(DCLocalePatch patch) {
        this.patch = patch;

        notifyDataSetChanged();
    }

    public void setPickedItem(int position) {
        this.pickedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return subfiles.size();
    }

    @Override
    public DCLocalePatch.Subfile getItem(int position) {
        return subfiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_subfile, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.label = convertView.findViewById(R.id.label);
            holder.sublabel = convertView.findViewById(R.id.sub_label);
            holder.indexlabel = convertView.findViewById(R.id.index_label);
            convertView.setTag(holder);
        }

        DCLocalePatch.Subfile subfile = getItem(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        convertView.setBackgroundColor(position == pickedPosition ? Color.DKGRAY : Color.TRANSPARENT);
        holder.label.setText(subfile.getHash().toUpperCase());
        if(patch != null) {
            holder.sublabel.setText("Files: "+patch.getHashFileDict(subfile.getHash()).size()+"/"+subfile.getDict().size());
        }else {
            holder.sublabel.setText("Files: "+subfile.getDict().size()+"");
        }
        holder.indexlabel.setText(String.valueOf(position+1));

        return convertView;
    }

    static class ViewHolder {
        TextView label, sublabel, indexlabel;
    }
}
