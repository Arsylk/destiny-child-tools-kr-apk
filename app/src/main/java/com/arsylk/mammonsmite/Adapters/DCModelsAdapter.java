package com.arsylk.mammonsmite.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.arsylk.mammonsmite.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class DCModelsAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<DCModelItem> srcModels = null;
    private List<DCModelItem> models = null;

    public DCModelsAdapter(Context context, File dir) {
        this.context = context;
        srcModels = new ArrayList<>();
        if(!dir.isDirectory()) {
            models = new ArrayList<>();
            return;
        }
        for(File file : dir.listFiles()) {
            srcModels.add(new DCModelItem(file));
        }
        Collections.sort(srcModels, new Comparator<DCModelItem>() {
            @Override
            public int compare(DCModelItem item1, DCModelItem item2) {
                return item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName());
            }
        });
        models = srcModels;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public DCModelItem getItem(int pos) {
        return models.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.item_dcmodel, parent, false);
        DCModelItem dcPck = getItem(pos);

        TextView label = convertView.findViewById(R.id.label);
        TextView sublabel = convertView.findViewById(R.id.sub_label);

        label.setText(dcPck.getFormatted());
        sublabel.setText(dcPck.isLoaded() ? dcPck.getFile().getName() : dcPck.getFile().getName());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                FilterResults results = new FilterResults();
                List<DCModelItem> filterList = new ArrayList<>();
                if(query != null) {
                    String fixQuery = query.toString().toLowerCase();
                    for(DCModelItem item : srcModels) {
                        if(item.getFormatted().toLowerCase().contains(fixQuery) || item.getFile().getName().toLowerCase().contains(fixQuery)) {
                            filterList.add(item);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                }else {
                    results.count = srcModels.size();
                    results.values = srcModels;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if(results != null)
                    if(results.values != null)
                        models = (ArrayList<DCModelItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
