package com.arsylk.dcwallpaper.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.dcwallpaper.Async.AsyncWithDialog;
import com.arsylk.dcwallpaper.Async.CachedImage;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_ITEM_TYPE;

public class DCWikiEquipmentAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private Set<Integer> toggles;
    private List<DCWiki.Equipment> srcWikiEquipment, wikiEquipment;
    private String filterString = "";


    public DCWikiEquipmentAdapter(Context context) {
        this.context = context;
        this.toggles = new HashSet<>();
        this.srcWikiEquipment = new ArrayList<>(LoadAssets.getDCWikiInstance().getEquipmentWiki());
        this.wikiEquipment = new ArrayList<>(LoadAssets.getDCWikiInstance().getEquipmentWiki());
    }

    public void cacheBitmaps() {
        // start all image caching tasks
        for(DCWiki.Equipment equipment : srcWikiEquipment) {
            equipment.getImage().asyncLoad(new Utils.OnPostExecute<CachedImage>() {
                @Override
                public void onPostExecute(CachedImage cachedImage) {
                    notifyDataSetChanged();
                }
            });
        }
    }

    public void toggleParameter(int id) {
        if(toggles.contains(id)) {
            toggles.remove(id);
        }else {
            toggles.add(id);
        }
        getFilter().filter(filterString);
    }

    static class ViewHolder {
        TextView name;
        TextView[] stats;
        ImageView icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_wiki_equipment, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.wiki_image_icon);
            holder.name = convertView.findViewById(R.id.wiki_item_name);
            holder.stats = new TextView[2];
            holder.stats[0] = convertView.findViewById(R.id.wiki_item_stat_1);
            holder.stats[1] = convertView.findViewById(R.id.wiki_item_stat_2);
            convertView.setTag(holder);
        }

        final DCWiki.Equipment equipment = getItem(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.name.setText(equipment.getName());
        holder.name.setTextColor(Color.HSVToColor(new float[] {(120.f * (equipment.getPower() / 100.f)), 1f, 0.9f}));
        for(int i = 0; i < holder.stats.length; i++) {
            if(i < equipment.getStats(true).size()) {
                DCWiki.Stat stat = equipment.getStats(true).get(i);
                holder.stats[i].setText(String.format("%s: %d", stat.shortText, stat.value));
            }else {
                holder.stats[i].setText("");
            }
        }

        if(equipment.getImage().getImageBitmap() != null) {
            holder.icon.setImageBitmap(equipment.getImage().getImageBitmap());
        }else  {
            holder.icon.setImageResource(android.R.color.transparent);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return wikiEquipment.size();
    }

    @Override
    public DCWiki.Equipment getItem(int position) {
        return wikiEquipment.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                filterString = query.toString().toLowerCase();
                FilterResults results = new FilterResults();
                List<DCWiki.Equipment> filterList = new ArrayList<>();
                mainloop:
                for(DCWiki.Equipment equipment : srcWikiEquipment) {
                    // check toggled item types
                    if(toggles.contains(CONVERT_ID_ITEM_TYPE[equipment.getType()])) {
                        continue;
                    }
                    // check all toggled stats
                    for(DCWiki.Stat stat : equipment.getStats(true)) {
                        if(toggles.contains(stat.statId)) {
                            continue mainloop;
                        }
                    }
                    // check filter string
                    if(!equipment.getName().toLowerCase().contains(filterString)) {
                        continue;
                    }

                    // add to filtered
                    filterList.add(equipment);
                }
                results.count = filterList.size();
                results.values = filterList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if(results != null)
                    if(results.values != null)
                        wikiEquipment = (List<DCWiki.Equipment>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
