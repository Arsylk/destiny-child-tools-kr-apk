package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import static com.arsylk.dcwallpaper.utils.Define.CONVERT_ID_TYPE;

public class DCWikiPageActivity extends AppCompatActivity implements FutureCallback<DCWiki.Page> {
    private Context context = DCWikiPageActivity.this;
    private String modelId;
    private DCWiki.Page wikiItem;

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

    private void asyncLoadWiki(final FutureCallback<DCWiki.Page> callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DCWiki.Page wikiPage = LoadAssets.getDCWikiInstance().getWikiPage(modelId);
                callback.onCompleted((wikiPage == null) ? new Exception("No wiki entry found") : null, wikiPage);
            }
        });
    }

    @Override
    public void onCompleted(Exception e, DCWiki.Page result) {
        if(e == null) {
            Log.d("mTag:Wiki", "Found wiki page");
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
                        toolbar.setTitle(wikiItem.getKname());
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

        if(wikiItem.getThumbnailImage() != null) {
            Ion.with(context).load(wikiItem.getThumbnailImage())
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
