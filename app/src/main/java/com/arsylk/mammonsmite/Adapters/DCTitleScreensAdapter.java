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

import static com.arsylk.mammonsmite.utils.Define.PATTERN_TITLE_SCREEN;

public class DCTitleScreensAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<DCTitleScreenItem> srcTitleScreens = null;
    private List<DCTitleScreenItem> titleScreens = null;

    public DCTitleScreensAdapter(Context context, File dir) {
        this.context = context;

        srcTitleScreens = new ArrayList<>();
        if(!dir.isDirectory()) {
            titleScreens = new ArrayList<>();
            return;
        }
        for(File file : dir.listFiles()) {
            if(PATTERN_TITLE_SCREEN.matcher(file.getName().toLowerCase()).matches()) {
                srcTitleScreens.add(new DCTitleScreenItem(file));
            }
        }
        Collections.sort(srcTitleScreens, (item1, item2) -> item1.getFile().getName().compareToIgnoreCase(item2.getFile().getName()));
        titleScreens = srcTitleScreens;
    }

    @Override
    public int getCount() {
        return titleScreens.size();
    }

    @Override
    public DCTitleScreenItem getItem(int pos) {
        return titleScreens.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_dctitle_screen, parent, false);
        DCTitleScreenItem titleScreen = getItem(pos);

        TextView label = convertView.findViewById(R.id.label);
        TextView sublabel = convertView.findViewById(R.id.sub_label);

        label.setText(titleScreen.getFormatted());
        sublabel.setText(titleScreen.getFile().getName());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                FilterResults results = new FilterResults();
                List<DCTitleScreenItem> filterList = new ArrayList<>();
                if(query != null) {
                    String fixQuery = query.toString().toLowerCase();
                    for(DCTitleScreenItem item : srcTitleScreens) {
                        if(item.getFormatted().toLowerCase().contains(fixQuery) || item.getFile().getName().toLowerCase().contains(fixQuery)) {
                            filterList.add(item);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                }else {
                    results.count = srcTitleScreens.size();
                    results.values = srcTitleScreens;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if(results != null)
                    if(results.values != null)
                        titleScreens = (ArrayList<DCTitleScreenItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
