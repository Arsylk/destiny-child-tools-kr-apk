package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.activities.DCWikiPageActivity;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.koushikdutta.ion.Ion;
import java.util.*;
import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_ELEMENT;
import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_TYPE;

public class DCWikiPagesAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private String filterString = "";
    private Set<Integer> toggles;
    private List<DCWiki.Page> srcWikiPages, wikiPages;

    public DCWikiPagesAdapter(Context context) {
        this.context = context;
        this.toggles = new HashSet<>();
        this.srcWikiPages = new ArrayList<>(LoadAssets.getDCWikiInstance().getWikiPages());
        this.wikiPages = new ArrayList<>(LoadAssets.getDCWikiInstance().getWikiPages());
    }

    @Override
    public int getCount() {
        return wikiPages.size();
    }

    @Override
    public DCWiki.Page getItem(int position) {
        return wikiPages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_wiki_page, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.label = convertView.findViewById(R.id.label);
            holder.sublabel = convertView.findViewById(R.id.sub_label);
            holder.element = convertView.findViewById(R.id.element);
            holder.type = convertView.findViewById(R.id.type);
            holder.thumbnail = convertView.findViewById(R.id.thumbnail);
            holder.stars = convertView.findViewById(R.id.thumbnail_stars_layout);
            convertView.setTag(holder);
        }

        final DCWiki.Page wikiPage = getItem(position);
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.label.setText(wikiPage.getName());
        holder.sublabel.setText(wikiPage.getModelId());
        holder.element.setImageResource(wikiPage.getElementDrawable());
        holder.type.setImageResource(wikiPage.getTypeDrawable());
        Ion.with(holder.thumbnail).load(wikiPage.getThumbnailImage());
        for(int i = 0; i < holder.stars.getChildCount(); i++) {
            holder.stars.getChildAt(i).setVisibility((i < wikiPage.getStars()) ? View.VISIBLE : View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, DCWikiPageActivity.class).putExtra("model_id", wikiPage.getModelId()));
            }
        });


        return convertView;
    }

    static class ViewHolder {
        TextView label, sublabel;
        ImageView element, type, thumbnail;
        ViewGroup stars;
    }

    public void toggleParameter(int id) {
        if(toggles.contains(id)) {
            toggles.remove(id);
        }else {
            toggles.add(id);
        }
        getFilter().filter(filterString);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                filterString = query.toString().toLowerCase();
                System.out.println(filterString);
                FilterResults results = new FilterResults();
                List<DCWiki.Page> filterList = new ArrayList<>();
                for(DCWiki.Page page : srcWikiPages) {
                    if(!toggles.contains(CONVERT_ID_ELEMENT[page.getElement()]) && !toggles.contains(CONVERT_ID_TYPE[page.getType()])) {
                        if(filterString != null) {
                            if(page.getName().toLowerCase().contains(filterString) || page.getKname().toLowerCase().contains(filterString)) {
                                filterList.add(page);
                            }
                        }else {
                            filterList.add(page);
                        }
                    }
                }
                results.count = filterList.size();
                results.values = filterList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if(results != null)
                    if(results.values != null)
                        wikiPages = (List<DCWiki.Page>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
