package com.arsylk.mammonsmite.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.arsylk.mammonsmite.DestinyChild.DCWiki;
import com.arsylk.mammonsmite.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class DCWikiPageActivity extends ActivityWithExceptionRedirect implements FutureCallback<DCWiki.Child> {
    private Context context = DCWikiPageActivity.this;
    private String modelId;
    private DCWiki.Child wikiItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcwiki_page);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_wiki));
        if(getIntent().hasExtra("model_id")) {
            modelId = getIntent().getStringExtra("model_id");
        }else {
            Toast.makeText(context, "No model id found!", Toast.LENGTH_SHORT).show();
            finish();
        }
        asyncLoadWiki(this);
    }

    private void asyncLoadWiki(final FutureCallback<DCWiki.Child> callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DCWiki.Child wikiChild = DCWiki.getInstance().getChildWiki(modelId);
                callback.onCompleted((wikiChild == null) ? new Exception("No wiki entry found") : null, wikiChild);
            }
        });
    }

    @Override
    public void onCompleted(Exception e, DCWiki.Child result) {
        if(e == null) {
            Log.d("mTag:Wiki", "Found wiki page");
            Log.d("mTag:Wiki", result.getKrName()+" - "+result.getName());
            wikiItem = result;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initViews();
                }
            });
        }else {
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        final ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setTitle(wikiItem.getName());
            findViewById(R.id.toolbar_wiki).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(toolbar.getTitle().toString().equalsIgnoreCase(wikiItem.getName())) {
                        toolbar.setTitle(wikiItem.getKrName());
                    }else {
                        toolbar.setTitle(wikiItem.getName());
                    }
                }
            });
        }

        TextView wiki_skill_leader, wiki_skill_auto, wiki_skill_tap, wiki_skill_slide, wiki_skill_drive;
        wiki_skill_leader = findViewById(R.id.wiki_skill_leader);
        wiki_skill_auto = findViewById(R.id.wiki_skill_auto);
        wiki_skill_tap = findViewById(R.id.wiki_skill_tap);
        wiki_skill_slide = findViewById(R.id.wiki_skill_slide);
        wiki_skill_drive = findViewById(R.id.wiki_skill_drive);

        ImageView wiki_thumbnail = findViewById(R.id.wiki_image_thumbnail);
        ImageView[] wiki_images = new ImageView[]{
                findViewById(R.id.wiki_image_1),
                findViewById(R.id.wiki_image_2),
                findViewById(R.id.wiki_image_3)
        };

        wiki_skill_leader.setText(wikiItem.getSkillLeader());
        wiki_skill_auto.setText(wikiItem.getSkillAuto());
        wiki_skill_tap.setText(wikiItem.getSkillTap());
        wiki_skill_slide.setText(wikiItem.getSkillSlide());
        wiki_skill_drive.setText(wikiItem.getSkillDrive());

        if(wikiItem.getImage().getUrl() != null) {
            Ion.with(context).load(wikiItem.getImage().getUrl())
                    .progressBar((ProgressBar) findViewById(R.id.wiki_image_thumbnail_progress))
                    .intoImageView(wiki_thumbnail).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    if(e == null) {
                        ((ImageView) findViewById(R.id.wiki_image_type))
                                .setImageResource(wikiItem.getTypeDrawable());
                    }else {
                        e.printStackTrace();
                    }
                }
            });
        }

        String[] portaitImages = wikiItem.getPortaitImages();
        for(int i = 0; i < portaitImages.length; i++) {
            if(portaitImages[i] != null) {
                Ion.with(context).load(portaitImages[i]).intoImageView(wiki_images[i]);
            }else {
                wiki_images[i].setVisibility(View.GONE);
            }
        }
    }
}
