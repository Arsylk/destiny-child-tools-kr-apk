package com.arsylk.dcwallpaper.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.DCAnnouncementItem;
import com.arsylk.dcwallpaper.Adapters.DCAnnouncementsAdapter;
import com.arsylk.dcwallpaper.Async.*;
import com.arsylk.dcwallpaper.Async.interfaces.OnPackFinishedListener;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.ion.Ion;

import java.io.File;

import static com.arsylk.dcwallpaper.utils.Define.REQUEST_FILE_PACK;
import static com.arsylk.dcwallpaper.utils.Define.REQUEST_FILE_UNPACK;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Context context = MainActivity.this;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ListView announcementList;
    private DCAnnouncementsAdapter adapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && (requestCode == REQUEST_FILE_PACK || requestCode == REQUEST_FILE_UNPACK)) {
            //URI random stuffs
            File file = null;
            try {
                Uri uri = data.getData();
                String uriPath = uri.getPath();
                System.out.println(uriPath);
                String[] parts = uriPath.split(":");
                if(parts.length > 1) {
                    uriPath = uriPath.substring(parts[0].length()+1);
                    uriPath = Environment.getExternalStorageDirectory() + File.separator + uriPath;
                    System.out.println(uriPath);
                }
                file = new File(uriPath);
            }catch(Exception e) {
                e.printStackTrace();
            }
            if(file == null || !file.exists())
                return;
            switch(requestCode) {
                case REQUEST_FILE_UNPACK: {
                    DCTools.asyncUnpack(file, context, new OnUnpackFinishedListener() {
                        @Override
                        public void onFinished(DCModel dcModel) {
                            if(dcModel != null) {
                                DCModelsActivity.showPickAction(context, dcModel);
                            }else {
                                Toast.makeText(context, "Failed to unpack!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
                }
                case REQUEST_FILE_PACK: {
                    DCTools.asyncPack(file, null, context, new OnPackFinishedListener() {
                        @Override
                        public void onFinished(File file) {
                            if(file != null) {
                                Toast.makeText(context, "Packed to: "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "Failed to pack!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    break;
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Utils.requestPermission(context);
        Utils.initDirectories();
        Ion.getDefault(context).getConscryptMiddleware().enable(false);
        initViews();
        if(!handleIntent()) {
            //check for updates
            LoadAssets.guiFullLoad(context);
            //load up-to-date banners
            new AsyncBanners(context, false).execute();
        }
    }

    private boolean handleIntent() {
        if(Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            DCTools.asyncUnpack(new File(getIntent().getData().getPath()), context, new OnUnpackFinishedListener() {
                @Override
                public void onFinished(DCModel dcModel) {
                    if(dcModel != null) {
                        DCModelsActivity.showPickAction(context, dcModel);
                    }else {
                        Toast.makeText(context, "Failed to unpack!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.dcmodels_open:
                openDCModels();
                break;
            case R.id.l2dmodels_open:
                openL2DModels();
                break;
            case R.id.onlinemodels_open:
                openOnlineModels();
                break;
            case R.id.file_unpack:
                pickFileIntent(REQUEST_FILE_UNPACK);
                break;
            case R.id.file_pack:
                pickFileIntent(REQUEST_FILE_PACK);
                break;
            case R.id.wiki_open:
                openDCWiki();
                break;
            case R.id.english_patch:
                openEnglishPatcher();
                break;
            case R.id.dcbanner_widget_open:
                openDCBannerWidget();
                break;
            case R.id.discord_open:
                openDiscord();
        }

        if(drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void initViews() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
        }

        drawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.main_navigation_view);

        drawerLayout.setScrimColor(Color.TRANSPARENT);
        navigationView.setNavigationItemSelectedListener(this);

        adapter = new DCAnnouncementsAdapter(context, true);

        announcementList = findViewById(R.id.main_announcements_list);
        announcementList.setAdapter(adapter);
        announcementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(adapter.getItem(position).getUrl())));
                }
            }
        });
        announcementList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter != null) {
                    DCAnnouncementItem announcement = adapter.getItem(position);
                    announcement.setShowTranslated(!announcement.isShowTranslated());
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    private void openDCModels() {
        if(LoadAssets.updateInProgress(context)) {
            Toast.makeText(context, "Wait for update to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(DCTools.getDCModelsPath().exists()) {
            startActivity(new Intent(context, DCModelsActivity.class));
        }else {
            Toast.makeText(context, "Destiny Child Kr not installed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openL2DModels() {
        startActivity(new Intent(context, L2DModelsActivity.class));
    }

    private void openOnlineModels() {
        startActivity(new Intent(context, OnlineModelsActivity.class));
    }

    private void pickFileIntent(int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        startActivityForResult(Intent.createChooser(intent, "Pick file"), requestCode);
    }

    private void openEnglishPatcher() {
        if(LoadAssets.updateInProgress(context)) {
            Toast.makeText(context, "Wait for update to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
        DCTools.asyncEnglishPatch(new File(DCTools.getDCLocalePath()), context);
    }

    private void openDCWiki() {
        if(LoadAssets.updateInProgress(context)) {
            Toast.makeText(context, "Wait for update to finish!", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(context, DCWikiActivity.class));
    }

    private void openDCBannerWidget() {
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        startActivityForResult(pickIntent, 293);
    }

    private void openDiscord() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/wDdq7C8")));
    }
}
