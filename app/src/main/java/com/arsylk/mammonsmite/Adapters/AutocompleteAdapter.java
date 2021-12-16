package com.arsylk.mammonsmite.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.mammonsmite.DestinyChild.DCNewWiki;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AutocompleteAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private LinkedHashMap<String, String> items;
    private ArrayList<String> keyList, valueList;

    public AutocompleteAdapter(Context context, LinkedHashMap<String, String> items) {
        this.context = context;
        this.items = items;
        this.keyList = new ArrayList<>(Utils.forloop(items.keySet().iterator()));
        this.valueList = new ArrayList<>(Utils.forloop(items.values().iterator()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ContextCompat.getSystemService(context, LayoutInflater.class)
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String idx = keyList.get(position), logic = valueList.get(position), name = "???", icon = "???";
        if(DCNewWiki.SKILL_TEXT_DATA.has(idx)) {
            try {
                String[] buff_text_parts = DCNewWiki.SKILL_TEXT_DATA.getString(idx).split("\t");
                if(buff_text_parts.length > 0)
                    name = buff_text_parts[0];
                if(buff_text_parts.length > 1)
                    icon = buff_text_parts[1];
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        File buffIconFile = new File(DCTools.Resources.DC_FILES_DIRECTORY, String.format("effect/battle/buff/%s/img/value.png", idx));
        Bitmap bitmap = BitmapFactory.decodeFile(buffIconFile.getAbsolutePath());

        SpannableStringBuilder ssb = new SpannableStringBuilder(String.format(" %s - %s\n%s - %s", idx, logic, icon, name));
        ssb.setSpan(new ImageSpan(context, bitmap), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(ssb, TextView.BufferType.SPANNABLE);

        return convertView;
    }

    @Override
    public String getItem(int position) {
        LinkedHashMap<String, String> temp = new LinkedHashMap<>();
        temp.put(keyList.get(position), valueList.get(position));
        return temp.entrySet().iterator().next().getValue();
    }

    @Override
    public int getCount() {
        return Math.min(keyList.size(), valueList.size());
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Map.Entry<String, String>> filterList = new ArrayList<>();
                String query = constraint.toString().toLowerCase();
                for(Map.Entry<String, String> entry : items.entrySet()) {
                    if(entry.getKey().toLowerCase().contains(query) || entry.getValue().toLowerCase().contains(query))
                        filterList.add(entry);
                    else if(query.isEmpty()) filterList.add(entry);
                }

                results.count = filterList.size();
                results.values = filterList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null) {
                    if(results.values != null) {
                        List<Map.Entry<String, String>> filteredDataset = (List<Map.Entry<String, String>>) results.values;
                        keyList = new ArrayList<>();
                        valueList = new ArrayList<>();

                        for(Map.Entry<String, String> entry : filteredDataset) {
                            keyList.add(entry.getKey());
                            valueList.add(entry.getValue());
                        }
                    }
                }
                notifyDataSetChanged();
            }
        };
    }

}
