package com.arsylk.mammonsmite.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.*;
import android.widget.*;
import com.arsylk.mammonsmite.DestinyChild.DCNewWiki;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Utils;

import java.io.File;
import java.util.*;

import static com.arsylk.mammonsmite.utils.Define.CHILD_ATTRIBUTE_OVERLAY;
import static com.arsylk.mammonsmite.utils.Define.CHILD_ATTRIBUTE_ICON;
import static com.arsylk.mammonsmite.utils.Define.CHILD_ROLE_ICON;


public class DCNewWikiAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<DCNewWiki.Child> baseDataset, filteredDataset;
    private Controller controller;


    public DCNewWikiAdapter(Context context) {
        this.context = context;
        this.baseDataset = new ArrayList<>();
        this.filteredDataset = new ArrayList<>();
        this.controller = new Controller(context, this);
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


        // get dump data & view holder
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final DCNewWiki.Child child = getItem(position);

        // fill data
        holder.label.setText(child.getName());
        holder.sublabel.setText(child.getSkin());
        holder.type.setImageResource(CHILD_ROLE_ICON[child.role]);
        holder.element.setImageResource(CHILD_ATTRIBUTE_ICON[child.attribute]);
        holder.frame.setImageResource(CHILD_ATTRIBUTE_OVERLAY[child.attribute]);
        if(child.getIcon() != null) {
            if(child.getIcon().isLoaded()) {
                holder.thumbnail.setImageBitmap(child.getIcon().getImageBitmap());
            }else {
                holder.thumbnail.setImageResource(android.R.color.transparent);
                child.getIcon().asyncLoad((x) -> notifyDataSetChanged());
            }
        } else  {
            holder.thumbnail.setImageResource(android.R.color.transparent);
        }
        for(int i = 0; i < holder.stars.getChildCount(); i++) {
            holder.stars.getChildAt(i).setVisibility((i < child.grade) ? View.VISIBLE : View.GONE);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView label, sublabel;
        ImageView element, type, thumbnail, frame;
        ViewGroup stars;
    }

    public void setItems(List<DCNewWiki.Child> children) {
        this.baseDataset = children;
        this.filteredDataset = children;
        notifyDataSetChanged();

        // TODO TESTING
        LinkedHashMap<String, String> nnn = DCNewWiki.getBuffLogicList();
        ArrayAdapter zzz = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, Utils.forloop(nnn.keySet().iterator())) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                TextView t = v.findViewById(android.R.id.text1);
                String logic = t.getText().toString();
                File buffIconFile = new File(DCTools.Resources.DC_FILES_DIRECTORY, String.format("effect/battle/buff/%s/img/value.png", nnn.get(logic)));
                Bitmap bitmap = BitmapFactory.decodeFile(buffIconFile.getAbsolutePath());

                SpannableStringBuilder ssb = new SpannableStringBuilder("  "+logic);
                ssb.setSpan(new ImageSpan(context, bitmap), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                t.setText(ssb, TextView.BufferType.SPANNABLE);

                return v;
            }
        };
        controller.buffLogicMenu.setAdapter(zzz);
    }

    @Override
    public DCNewWiki.Child getItem(int position) {
        return filteredDataset.get(position);
    }

    @Override
    public int getCount() {
        return filteredDataset.size();
    }

    @Override
    public long getItemId(int position) {
        if(getItem(position) != null)
            return Long.parseLong(getItem(position).idx);
        return 0;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Controller.Filter filter = controller.getFilter();
                FilterResults results = new FilterResults();
                List<DCNewWiki.Child> filterList = new ArrayList<>();

                for(DCNewWiki.Child child : baseDataset)
                    if(filter.check(child))
                        filterList.add(child);

                results.count = filterList.size();
                results.values = filterList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null)
                    if(results.values != null)
                        filteredDataset = (List<DCNewWiki.Child>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public static class Controller {
        private Context context;
        private DCNewWikiAdapter adapter;
        private AlertDialog dialog = null;
        private View view;
        private ViewGroup starsLayout, attributesLayout, rolesLayout;
        private TextView basicSearch, skillsSearch;
        private AutoCompleteTextView buffLogicMenu;
        private TextView exactIdx, exactSkillIdx, exactBuffIdx;


        public Controller(Context context, DCNewWikiAdapter adapter) {
            this.context = context;
            this.adapter = adapter;
            initViews();
        }

        private void initViews() {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_new_wiki_controller, null, false);

            // star search UI
            starsLayout = view.findViewById(R.id.search_stars_layout);
            for(int i = 0; i < starsLayout.getChildCount(); i++) {
                starsLayout.getChildAt(i).setOnClickListener(view -> {
                    int stars = Integer.parseInt(view.getTag().toString());
                    for(int i1 = 1; i1 < starsLayout.getChildCount(); i1++) {
                        starsLayout.getChildAt(i1).setAlpha((i1 <= stars) ? 1.0f : 0.5f);
                    }
                });
            }
            // attribute search UI
            attributesLayout = view.findViewById(R.id.search_attribute_layout);
            for(int i = 0; i < attributesLayout.getChildCount(); i++) {
                attributesLayout.getChildAt(i).setOnClickListener(view -> {
                    int attribute = Integer.parseInt(view.getTag().toString());
                    view.setAlpha(view.getAlpha() !=  1.0f ? 1.0f : 0.5f);
                });
            }
            // roles search UI
            rolesLayout = view.findViewById(R.id.search_role_layout);
            for(int i = 0; i < rolesLayout.getChildCount(); i++) {
                rolesLayout.getChildAt(i).setOnClickListener(view -> {
                    int role = Integer.parseInt(view.getTag().toString());
                    view.setAlpha(view.getAlpha() !=  1.0f ? 1.0f : 0.5f);
                });
            }

            // search input's
            basicSearch = view.findViewById(R.id.search_input_basic);
            skillsSearch = view.findViewById(R.id.search_input_skills);

            // buff logic menu
            buffLogicMenu = view.findViewById(R.id.search_menu_buff_logic);
            buffLogicMenu.setOnClickListener((v) -> buffLogicMenu.showDropDown());

            // exact search input's
            exactIdx = view.findViewById(R.id.search_input_idx);
            exactSkillIdx = view.findViewById(R.id.search_input_skill_idx);
            exactBuffIdx = view.findViewById(R.id.search_input_buff_idx);
        }

        public void showControllerPopup() {
            if(dialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);
                builder.setPositiveButton("Ok", (dialog, which) -> {
                    adapter.getFilter().filter("meme");
                });
                builder.setNegativeButton("Close", (dialog, which) -> {
                    dialog.dismiss();
                });
                dialog = builder.create();
            }
            dialog.show();
        }

        public Filter getFilter() {
            Filter filter = new Filter();

            // text search
            filter.basicSearch = basicSearch.getText().toString();
            filter.skillsSearch = skillsSearch.getText().toString();
            filter.logicSearch = buffLogicMenu.getText().toString();
            filter.exactIdx = exactIdx.getText().toString();
            filter.exactSkillIdx = exactSkillIdx.getText().toString();
            filter.exactBuffIdx = exactBuffIdx.getText().toString();


             filter.checkIgnited = filter.skillsSearch.startsWith("$");
             if(filter.checkIgnited)
                 filter.skillsSearch = filter.skillsSearch.substring(1);

            // stars selector
            filter.enabledStars = new HashSet<>();
            for(int i = 0; i < starsLayout.getChildCount(); i++) {
                View starView = starsLayout.getChildAt(i);
                if(starView.getAlpha() == 1.0f) {
                    int star = Integer.parseInt(starView.getTag().toString());
                    filter.enabledStars = new HashSet<>();
                    filter.enabledStars.add(star);
                }
            }
            if(filter.enabledStars.isEmpty()) filter.enabledStars.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));

            // attributes selector
            filter.enabledAttributes = new HashSet<>();
            for(int i = 0; i < attributesLayout.getChildCount(); i++) {
                View attributeView = attributesLayout.getChildAt(i);
                if(attributeView.getAlpha() == 1.0f) {
                    int attribute = Integer.parseInt(attributeView.getTag().toString());
                    filter.enabledAttributes.add(attribute);
                }
            }
            if(filter.enabledAttributes.isEmpty()) filter.enabledAttributes.addAll(Arrays.asList(1, 2, 3, 4, 5));

            // roles selector
            filter.enabledRoles = new HashSet<>();
            for(int i = 0; i < rolesLayout.getChildCount(); i++) {
                View roleView = rolesLayout.getChildAt(i);
                if(roleView.getAlpha() == 1.0f) {
                    int role = Integer.parseInt(roleView.getTag().toString());
                    filter.enabledRoles.add(role);
                }
            }
            if(filter.enabledRoles.isEmpty()) filter.enabledRoles.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));


            return filter;
        }

        static class Filter {
            String basicSearch, skillsSearch, logicSearch;
            String exactIdx, exactSkillIdx, exactBuffIdx;
            Set<Integer> enabledStars, enabledAttributes, enabledRoles;
            boolean checkIgnited;

            boolean check(DCNewWiki.Child child) {
                boolean matches_basic = basicSearch.isEmpty();
                boolean matches_skills = skillsSearch.isEmpty();
                boolean matches_logic = logicSearch.isEmpty();
                boolean exact_idx = exactIdx.isEmpty(),
                        exact_skillIdx = exactSkillIdx.isEmpty(),
                        exact_buffIdx = exactBuffIdx.isEmpty();
                boolean qualifies = true;


                // matching to basic search
                if(!basicSearch.isEmpty()) {
                    if(child.name.toLowerCase().contains(basicSearch.toLowerCase()))
                        matches_basic = true;
                    for(Map.Entry<String, String> entry : child.skins.entrySet())
                        if(entry.getKey().toLowerCase().contains(basicSearch.toLowerCase()) ||
                                entry.getValue().toLowerCase().contains(basicSearch.toLowerCase()))
                            matches_basic = true;
                }

                // matching to skills search
                if(!skillsSearch.isEmpty()) {
                    for(DCNewWiki.Skill skill : child.skills.values())
                        for(DCNewWiki.SkillPart part : skill.parts)
                            if(part.buff_name.toLowerCase().contains(skillsSearch.toLowerCase()))
                                matches_skills = true;
                    // matching to ignited skills
                    if(checkIgnited)
                        for(DCNewWiki.Skill skill : child.skills_ignited.values())
                            for(DCNewWiki.SkillPart part : skill.parts)
                                if(part.buff_name.toLowerCase().contains(skillsSearch.toLowerCase()))
                                    matches_skills = true;
                }

                // matching to logic
                if(!logicSearch.isEmpty()) {
                    for(DCNewWiki.Skill skill : child.skills.values())
                        for(DCNewWiki.SkillPart part : skill.parts)
                            if(part.buff_logic.toLowerCase().contains(logicSearch.toLowerCase()))
                                matches_logic = true;
                    // matching to ignited logic
                    if(checkIgnited)
                        for(DCNewWiki.Skill skill : child.skills_ignited.values())
                            for(DCNewWiki.SkillPart part : skill.parts)
                                if(part.buff_logic.toLowerCase().contains(logicSearch.toLowerCase()))
                                    matches_logic = true;
                }

                // exact idx'es
                if(!exactIdx.isEmpty()) {
                    if(child.idx.contains(exactIdx))
                        exact_idx = true;
                }
                if(!exactSkillIdx.isEmpty()) {
                    for(DCNewWiki.Skill skill : child.skills.values())
                        if(skill.idx.contains(exactSkillIdx))
                            exact_skillIdx = true;
                    if(checkIgnited)
                        for(DCNewWiki.Skill skill : child.skills_ignited.values())
                            if(skill.idx.contains(exactSkillIdx))
                                exact_skillIdx = true;
                }
                if(!exactBuffIdx.isEmpty()) {
                    for(DCNewWiki.Skill skill : child.skills.values())
                        for(DCNewWiki.SkillPart part : skill.parts)
                            if(part.buff_idx.contains(exactBuffIdx))
                                exact_buffIdx = true;
                    if(checkIgnited)
                        for(DCNewWiki.Skill skill : child.skills_ignited.values())
                            for(DCNewWiki.SkillPart part : skill.parts)
                                if(part.buff_idx.contains(exactBuffIdx))
                                    exact_buffIdx = true;
                }


                // qualifying to selectors
                if(!enabledStars.contains(child.grade))
                    qualifies = false;
                if(!enabledAttributes.contains(child.attribute))
                    qualifies = false;
                if(!enabledRoles.contains(child.role))
                    qualifies = false;


                return matches_basic && matches_skills && matches_logic && qualifies && exact_idx && exact_skillIdx && exact_buffIdx;
            }
        }
    }
}
