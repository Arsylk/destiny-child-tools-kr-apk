package com.arsylk.dcwallpaper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.arsylk.dcwallpaper.Async.CachedImage;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;

import java.util.Arrays;
import java.util.List;

public class DCWikiSoulCartaAdapter extends RecyclerView.Adapter<DCWikiSoulCartaAdapter.Holder> {
    private Context context;
    protected List<DCWiki.SoulCarta> soulCartas;
    protected boolean[] soulCartasPrisma;

    public DCWikiSoulCartaAdapter(Context context) {
        this.context = context;
        soulCartas = LoadAssets.getDCWikiInstance().getSoulCartaWiki();
        soulCartasPrisma = new boolean[soulCartas.size()];
        Arrays.fill(soulCartasPrisma, false);
    }

    public void cacheBitmaps() {
        for(DCWiki.SoulCarta soulCarta : soulCartas) {
            soulCarta.getCarta().asyncLoad(new Utils.OnPostExecute<CachedImage>() {
                @Override
                public void onPostExecute(CachedImage cachedImage) {
                    notifyDataSetChanged();
                }
            });
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.item_wiki_soul_carta, viewGroup, false);
        return new DCWikiSoulCartaAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final int position = holder.getAdapterPosition();
        final DCWiki.SoulCarta soulCarta = soulCartas.get(position);
        final boolean prisma = soulCartasPrisma[position];

        holder.soulCarta.setImageBitmap(soulCarta.getCarta().getImageBitmap());
        holder.soulCartaPrisma.setVisibility(prisma && soulCarta.getCarta().getImageBitmap() != null ? View.VISIBLE : View.GONE);

        holder.name.setText(soulCarta.getName());
        holder.description.setText(soulCarta.getDescription());
        for(int s = 0; s < Math.min(holder.stats_text.length, holder.stats_value.length); s++) {
            if(s < soulCarta.getStats(prisma).size()) {
                DCWiki.Stat stat = soulCarta.getStats(prisma).get(s);
                holder.stats_text[s].setText(stat.shortText+":");
                holder.stats_value[s].setText(String.valueOf(stat.value));
            }
        }

        holder.element.setText(soulCarta.getElementText());
        holder.elementIcon.setImageResource(soulCarta.getElementDrawable());

        holder.type.setText(soulCarta.getTypeText());
        holder.typeIcon.setImageResource(soulCarta.getTypeDrawable());
        holder.conditions.setVisibility((soulCarta.getElementDrawable() != 0 || soulCarta.getTypeDrawable() != 0) ? View.VISIBLE : View.GONE);

        holder.skill.setText(soulCarta.getSkillFormatted(prisma));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soulCartasPrisma[position] = ! soulCartasPrisma[position];
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return soulCartas.size();
    }


    class Holder extends RecyclerView.ViewHolder {
        protected ImageView soulCarta, soulCartaPrisma;
        protected View conditions;
        protected TextView name, description, element, type;
        protected ImageView elementIcon, typeIcon;
        protected TextView[] stats_text;
        protected TextView[] stats_value;
        protected TextView skill;

        private Holder(View view) {
            super(view);
            soulCarta = view.findViewById(R.id.wiki_soul_carta);
            soulCartaPrisma = view.findViewById(R.id.wiki_soul_carta_prisma);

            name = view.findViewById(R.id.wiki_soul_carta_name);
            description = view.findViewById(R.id.wiki_soul_carta_description);

            stats_text = new TextView[2];
            stats_text[0] = view.findViewById(R.id.wiki_soul_carta_stats_text_1);
            stats_text[1] = view.findViewById(R.id.wiki_soul_carta_stats_text_2);
            stats_value = new TextView[2];
            stats_value[0] = view.findViewById(R.id.wiki_soul_carta_stats_value_1);
            stats_value[1] = view.findViewById(R.id.wiki_soul_carta_stats_value_2);

            conditions = view.findViewById(R.id.wiki_soul_carta_conditions);
            element = view.findViewById(R.id.wiki_soul_carta_element);
            elementIcon = view.findViewById(R.id.wiki_soul_carta_element_icon);
            type = view.findViewById(R.id.wiki_soul_carta_type);
            typeIcon = view.findViewById(R.id.wiki_soul_carta_type_icon);

            skill = view.findViewById(R.id.wiki_soul_carta_skill);
        }
    }
}