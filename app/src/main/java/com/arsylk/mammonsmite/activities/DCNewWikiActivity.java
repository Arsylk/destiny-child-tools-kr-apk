package com.arsylk.mammonsmite.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import com.arsylk.mammonsmite.Adapters.DCNewWikiAdapter;
import com.arsylk.mammonsmite.Async.AsyncWithDialog;
import com.arsylk.mammonsmite.DestinyChild.DCNewWiki;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.LoadAssets;
import com.arsylk.mammonsmite.views.BigTextDialog;
import com.koushikdutta.ion.Ion;


import static com.arsylk.mammonsmite.utils.Define.*;

public class DCNewWikiActivity extends AppCompatActivity {
    private Context context = DCNewWikiActivity.this;
    private ListView listView = null;
    private DCNewWikiAdapter adapter = null;
    private DCNewWikiAdapter.Controller controller = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcnew_wiki);
        setSupportActionBar(findViewById(R.id.toolbar));
        initViews();

        // start loading dump data
        AsyncWithDialog.execute(context, true, "Loading Dump Data...", () -> {
            try {
                for(String dump_data : DUMP_DATA)
                    LoadAssets.updateDumpData(context, dump_data).get();
                DCNewWiki.load();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }, () -> {
            adapter.setItems(DCNewWiki.ALL_CHILDREN);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_wiki_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        try {
            DCNewWiki.unload();
        }catch(Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void initViews() {
        ((Toolbar) findViewById(R.id.toolbar)).setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId() == R.id.new_wiki_menu_search)
                if(adapter != null && controller != null)
                    controller.showControllerPopup();
            return true;
        });


        adapter = new DCNewWikiAdapter(context);
        controller = adapter.getController();

        listView = findViewById(R.id.new_wiki_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            DCNewWiki.Child child = adapter.getItem(position);
            if(child != null) {
                makeChildDialog(child);
            }
        }));
    }

    private void makeChildDialog(DCNewWiki.Child child) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final PopupWindow dimPopup = new PopupWindow(new View(context), (int) (metrics.widthPixels * 0.9), (int) (metrics.heightPixels * 0.9), true);
        dimPopup.setBackgroundDrawable(new ColorDrawable(0xff000000));
        View childItemView = getLayoutInflater().inflate(R.layout.item_new_wiki_child, null, false);
        final PopupWindow zoomPopup = new PopupWindow(childItemView, (int) (metrics.widthPixels * 0.9), (int) (metrics.heightPixels * 0.9), false);


        // set action listeners
        View.OnClickListener onClickListener = v -> {
            zoomPopup.dismiss(); dimPopup.dismiss();
        };
        PopupWindow.OnDismissListener onDismiss = () -> {
            zoomPopup.dismiss(); dimPopup.dismiss();
        };
        dimPopup.getContentView().setOnClickListener(onClickListener);
        zoomPopup.getContentView().setOnClickListener(onClickListener);
        dimPopup.setOnDismissListener(onDismiss);
        zoomPopup.setOnDismissListener(onDismiss);

        // show both popup's
        dimPopup.showAtLocation(dimPopup.getContentView(), Gravity.CENTER, 0, 0);
        zoomPopup.showAtLocation(zoomPopup.getContentView(), Gravity.CENTER, 0, 0);

        // assign views
        TextView nameView = childItemView.findViewById(R.id.new_wiki_text_name);
        TextView idxView = childItemView.findViewById(R.id.new_wiki_text_idx);
        ImageView iconView = childItemView.findViewById(R.id.new_wiki_image_icon);
        ImageView overlayView = childItemView.findViewById(R.id.new_wiki_image_overlay);
        ImageView roleView = childItemView.findViewById(R.id.new_wiki_image_role);
        ViewGroup starsViewGroup = childItemView.findViewById(R.id.new_wiki_layout_stars);
        ViewGroup statusViewGroup = childItemView.findViewById(R.id.new_wiki_layout_status);
        TextView skinView = childItemView.findViewById(R.id.new_wiki_text_skin);
        ViewGroup skillsViewGroup = childItemView.findViewById(R.id.new_wiki_layout_skills);
        skillsViewGroup.removeAllViews();


        // load & display images
        Ion.with(iconView).load(String.format("http://arsylk.pythonanywhere.com/static/icons/%s.png", child.getSkin()));
        overlayView.setImageResource(CHILD_ATTRIBUTE_OVERLAY[child.attribute]);
        roleView.setImageResource(CHILD_ROLE_ICON[child.role]);
        for(int i = 0; i < starsViewGroup.getChildCount(); i++) {
            starsViewGroup.getChildAt(i).setVisibility((i < child.grade) ? View.VISIBLE : View.GONE);
        }

        // fill information header
        nameView.setText(child.getName());
        idxView.setText(child.idx);
        String formattedSkins = child.skins.keySet().toString();
        skinView.setText(formattedSkins.substring(1, formattedSkins.length() - 1));
        nameView.setOnLongClickListener((view) -> {
            try {
                String bigText = child.json.toString(4);
                BigTextDialog bigTextDialog = new BigTextDialog(context, "CHAR_DATA", bigText);
                bigTextDialog.setFilename(String.format("CHAR_DATA_%s.json", child.idx));
                bigTextDialog.show();
            }catch(Exception e) {
                e.printStackTrace();
            }

            return true;
        });

        // fill status values
        for(int i = 0; i < statusViewGroup.getChildCount(); i++) {
            Pair<String, Integer> status = child.status.get(i);
            String formattedStatus = String.format("%s: %d", status.first.substring(0, 1).toUpperCase() + status.first.substring(1), status.second);
            ((TextView) statusViewGroup.getChildAt(i)).setText(formattedStatus);
        }

        // generate skills
        try {
            makeSkillView(skillsViewGroup, child, child.getSkill(SKILL_AUTO));
            makeSkillView(skillsViewGroup, child, child.getSkill(SKILL_TAP));
            makeSkillView(skillsViewGroup, child, child.getSkill(SKILL_SLIDE));
            makeSkillView(skillsViewGroup, child, child.getSkill(SKILL_DRIVE));
            makeSkillView(skillsViewGroup, child, child.getSkill(SKILL_LEADER));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void makeSkillView(ViewGroup parent, DCNewWiki.Child child, DCNewWiki.Skill skill) {
        View skillView = getLayoutInflater().inflate(R.layout.item_new_wiki_skill, parent, false);
        fillSkillView(skill, skillView);

        skillView.setOnClickListener((view) -> {
            if(view.getTag() instanceof DCNewWiki.Skill) {
                DCNewWiki.Skill skillTag = (DCNewWiki.Skill) view.getTag();
                if(skillTag.ignited) {
                    fillSkillView(child.getSkill(skill.type), view);
                }else {
                    if(child.getIgnitedSkill(skill.type) != null)
                        fillSkillView(child.getIgnitedSkill(skill.type), view);
                }
            }
        });
        skillView.setOnLongClickListener((view) -> {
            if(view.getTag() instanceof DCNewWiki.Skill) {
                DCNewWiki.Skill skillTag = (DCNewWiki.Skill) view.getTag();
                try {
                    String bigText = skillTag.json.toString(4);
                    BigTextDialog bigTextDialog = new BigTextDialog(context, "SKILL_ACTIVE_DATA", bigText);
                    bigTextDialog.setFilename(String.format("SKILL_ACTIVE_DATA_%s.json", skillTag.idx));
                    bigTextDialog.show();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return true;
        });
        parent.addView(skillView);
    }

    private void fillSkillView(DCNewWiki.Skill skill, View view) {
        TextView skillIdxView = view.findViewById(R.id.new_wiki_item_skill_idx);
        TextView skillNameView = view.findViewById(R.id.new_wiki_item_skill_name);
        TextView skillTextView = view.findViewById(R.id.new_wiki_item_skill_text);
        ViewGroup skillBuffs = view.findViewById(R.id.new_wiki_item_skill_buffs);

        int[] buffViewIds = {
                R.id.new_wiki_item_skill_buff_0,
                R.id.new_wiki_item_skill_buff_1,
                R.id.new_wiki_item_skill_buff_2,
                R.id.new_wiki_item_skill_buff_3,
                R.id.new_wiki_item_skill_buff_4,
        };
        for(int i = 0; i < skill.parts.size(); i++) {
            DCNewWiki.SkillPart part = skill.parts.get(i);

            View buffView = skillBuffs.findViewById(buffViewIds[i]);
            if(!part.buff_idx.isEmpty()) {
                TextView buffDataView = buffView.findViewById(R.id.new_wiki_item_text_buff_data);
                TextView buffTextView = buffView.findViewById(R.id.new_wiki_item_text_buff_text);
                ImageView buffImageView = buffView.findViewById(R.id.new_wiki_item_image_buff_icon);

                buffDataView.setText(String.format("%s - %s", part.buff_idx, part.buff_logic));
                buffTextView.setText(String.format("%s - %s", part.buff_name, part.buff_description));
                buffImageView.setImageBitmap(part.getBuffIcon());
                buffView.setOnLongClickListener((v) -> {
                    try {
                        String bigText = part.buff_json.toString(4);
                        BigTextDialog bigTextDialog = new BigTextDialog(context, "SKILL_BUFF_DATA", bigText);
                        bigTextDialog.setFilename(String.format("SKILL_BUFF_DATA_%s.json", part.buff_idx));
                        bigTextDialog.show();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                });

                buffView.setVisibility(View.VISIBLE);
            }else buffView.setVisibility(View.GONE);
        }

        skillIdxView.setText(skill.idx);
        skillNameView.setText(skill.getName());
        skillTextView.setText(skill.getFilledText());
        skillNameView.setTextColor(skill.ignited ? Color.RED : Color.WHITE);
        view.setTag(skill);
    }
}
