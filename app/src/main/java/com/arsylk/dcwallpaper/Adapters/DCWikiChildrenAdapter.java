package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.dcwallpaper.Async.CachedImage;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.activities.DCWikiPageActivity;
import com.arsylk.dcwallpaper.utils.Utils;
import java.util.*;
import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_ELEMENT;
import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_TYPE;

public class DCWikiChildrenAdapter extends BaseAdapter implements Filterable, Utils.OnPostExecute<CachedImage> {
    private Context context;
    private String filterString = "";
    private int stars = 0;
    private Set<Integer> toggles;
    private List<DCWiki.Child> srcWikiChildren, wikiChildren;

    public DCWikiChildrenAdapter(Context context) {
        this.context = context;
        this.toggles = new HashSet<>();
        this.srcWikiChildren = new ArrayList<>(DCWiki.getInstance().getChildrenWiki());
        this.wikiChildren = new ArrayList<>(DCWiki.getInstance().getChildrenWiki());
    }

    @Override
    public void onPostExecute(CachedImage cachedImage) {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return wikiChildren.size();
    }

    @Override
    public DCWiki.Child getItem(int position) {
        return wikiChildren.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_wiki_child, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.label = convertView.findViewById(R.id.label);
            holder.sublabel = convertView.findViewById(R.id.sub_label);
            holder.element = convertView.findViewById(R.id.element);
            holder.type = convertView.findViewById(R.id.type);
            holder.thumbnail = convertView.findViewById(R.id.thumbnail);
            holder.frame = convertView.findViewById(R.id.thumbnail_frame);
            holder.stars = convertView.findViewById(R.id.thumbnail_stars_layout);
            convertView.setTag(holder);
        }

        // get wiki page & view
        final DCWiki.Child wikiChild = getItem(position);
        final ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.label.setText(wikiChild.getName());
        holder.sublabel.setText(wikiChild.getModelId());
        holder.element.setImageResource(wikiChild.getElementDrawable());
        holder.type.setImageResource(wikiChild.getTypeDrawable());
        if(wikiChild.getImage().isLoaded()) {
            holder.thumbnail.setImageBitmap(wikiChild.getImage().getImageBitmap());
        }else  {
            holder.thumbnail.setImageResource(android.R.color.transparent);
            wikiChild.getImage().asyncLoad(this);
        }
        holder.frame.setImageResource(wikiChild.getElementFrame());
        for(int i = 0; i < holder.stars.getChildCount(); i++) {
            holder.stars.getChildAt(i).setVisibility((i < wikiChild.getStars()) ? View.VISIBLE : View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, DCWikiPageActivity.class).putExtra("model_id", wikiChild.getModelId()));
            }
        });


        return convertView;
    }

    static class ViewHolder {
        TextView label, sublabel;
        ImageView element, type, thumbnail, frame;
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

    public void toggleStars(int stars) {
        this.stars = stars;
        getFilter().filter(filterString);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                filterString = query.toString().toLowerCase();
                FilterResults results = new FilterResults();
                List<DCWiki.Child> filterList = new ArrayList<>();
                for(DCWiki.Child child : srcWikiChildren) {
                    if(!toggles.contains(CONVERT_ID_ELEMENT[child.getElement()]) && !toggles.contains(CONVERT_ID_TYPE[child.getType()])) {
                        if(stars == 0 || stars == child.getStars()) {
                            if(filterString != null) {
                                if(child.getName().toLowerCase().contains(filterString) ||
                                        child.getKrName().toLowerCase().contains(filterString) ||
                                            child.getModelId().toLowerCase().contains(filterString)) {
                                    filterList.add(child);
                                }
                            }else {
                                filterList.add(child);
                            }
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
                        wikiChildren = (List<DCWiki.Child>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
