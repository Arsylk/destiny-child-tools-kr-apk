package com.arsylk.dcwallpaper.activities;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.DCAnnouncementItem;
import com.arsylk.dcwallpaper.Adapters.DCAnnouncementsAdapter;
import com.arsylk.dcwallpaper.Async.AsyncBanners;
import com.arsylk.dcwallpaper.Async.AsyncPatch;
import com.arsylk.dcwallpaper.Async.AsyncLoadAssets;
import com.arsylk.dcwallpaper.Async.AsyncVersionChecker;
import com.arsylk.dcwallpaper.Async.interfaces.OnPackFinishedListener;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.BuildConfig;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.loader.AsyncHttpRequestFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import static com.arsylk.dcwallpaper.utils.Define.*;

public class MainActivity extends ActivityWithExceptionRedirect implements NavigationView.OnNavigationItemSelectedListener {
    static final String TAG = "Activity/MainActivity";
    private Context context = MainActivity.this;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ListView announcementList;
    private DCAnnouncementsAdapter adapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle picked files
        if(data != null && (requestCode == REQUEST_FILE_PACK || requestCode == REQUEST_FILE_UNPACK)) {
            File file = Utils.uriToFile(data.getData());
            if(file == null || !file.exists())
                return;
            switch(requestCode) {
                case REQUEST_FILE_UNPACK: {
                    DCTools.asyncUnpack(file, context, new OnUnpackFinishedListener() {
                        @Override
                        public void onFinished(DCModel dcModel) {
                            if(dcModel != null) {
                                if(dcModel.isLoaded()) {
                                    DCModelsActivity.showPickAction(context, dcModel.asL2DModel());
                                }
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

        // request permission
        if(Utils.requestPermission(context)) {
           onCreatePermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // handle permissions
        Log.d(TAG, String.format("requestCode: %d, resultCode: %d", requestCode, 0));
        if(requestCode == REQUEST_PERMISSION_STORAGE) {
            // check for permissions again
            if(Utils.requestPermission(context)) {
                onCreatePermissionGranted();
            }
        }

    }

    // continue onCreate if permissions granted
    private void onCreatePermissionGranted() {
        // make sure all directories exists
        Utils.initDirectories();

        // setup apk-wide settings
        Locale.setDefault(Locale.US);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getBaseContext().getResources().getConfiguration().setLocale(Locale.US);
        } else {
            getBaseContext().getResources().getConfiguration().locale = Locale.US;
        }
        Ion.getDefault(context).getConscryptMiddleware().enable(false);
        Ion.getDefault(context).configure().setAsyncHttpRequestFactory(new AsyncHttpRequestFactory() {
            @Override
            public AsyncHttpRequest createAsyncHttpRequest(Uri uri, String method, Headers headers) {
                AsyncHttpRequest request = new AsyncHttpRequest(uri, method, headers);
                request.getHeaders().set("Apk-Name", BuildConfig.APPLICATION_ID);
                request.getHeaders().set("Apk-Version", BuildConfig.VERSION_NAME);
                request.getHeaders().set("Device-Token", Utils.getDeviceToken(context));
                return request;
            }
        });


        // init views & load resources
        initViews();
        if(!handleIntent()) {
            // check remote assets
            LoadAssets.guiFullLoad(context, new Utils.Callback() {
                @Override
                public void onCall() {
                    // load up-to-date announcements
                    adapter.loadAnnouncements();

                    // load up-to-date banners
                    new AsyncBanners(context, false).execute();
                }
            });

            // check application version
            new AsyncVersionChecker(context).execute();
        }
    }


    private boolean handleIntent() {
        if(Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            DCTools.asyncUnpack(new File(getIntent().getData().getPath()), context, new OnUnpackFinishedListener() {
                @Override
                public void onFinished(DCModel dcModel) {
                    if(dcModel != null) {
                        if(dcModel.isLoaded()) {
                            DCModelsActivity.showPickAction(context, dcModel.asL2DModel());
                        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if(BuildConfig.DEBUG)
            getMenuInflater().inflate(R.menu.developer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            //release menu
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.main_settings:
                startActivity(new Intent(context, SettingsActivity.class));
                return true;

            //dev menu
            case R.id.dev_md5:
                DCTools.fullFilesDump(context);
                return true;
            case R.id.dev_extract_new:
                DCTools.asyncExtractMissing(DCTools.getDCLocalePath(), context, true);
                return true;
            case R.id.dev_swap_all:
                DCTools.fullPckSwap(context, new L2DModel(new File(Define.MODELS_DIRECTORY, "yukine")));
                return true;
            case R.id.dev_swap_all_restore:
                File[] backups = DCTools.getDCModelsPath().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().startsWith("_") && file.getName().endsWith(".pck");
                    }
                });

                //restore model_info.json
                try {
                    File modelInfoBackup = new File(DCTools.getDCModelInfoPath().getParentFile(), "_"+DCTools.getDCModelInfoPath().getName());
                    if(modelInfoBackup.exists()) {
                        FileUtils.forceDelete(DCTools.getDCModelInfoPath());
                        FileUtils.moveFile(modelInfoBackup, DCTools.getDCModelInfoPath());
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }

                //restore pck files
                for(File backup : backups) {
                    try {
                        File file = new File(backup.getParentFile(), backup.getName().replaceFirst("_", ""));
                        FileUtils.forceDelete(file);
                        FileUtils.moveFile(backup, file);
                        Log.d("mTag:FullPck", "restored: "+file);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.dev_cause_exception:
                getIntent().getData().getAuthority().charAt(312);
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
            case R.id.dcmodels_swap:
                startActivity(new Intent(context, DCSwapActivity.class));
                break;
            case R.id.wiki_open:
                openDCWiki();
                break;
            case R.id.translate_locale:
                openTranslateLocale();
                break;
            case R.id.english_patch:
                openEnglishPatcher();
                break;
            case R.id.russian_patch:
                openRussianPatcher();
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
        navigationView.getMenu().findItem(R.id.menu_version).setTitle(String.format("%s v%s", BuildConfig.BUILD_TYPE, BuildConfig.VERSION_NAME));

        adapter = new DCAnnouncementsAdapter(context, false);

        announcementList = findViewById(R.id.main_announcements_list);
        announcementList.setAdapter(adapter);
        announcementList.addFooterView(adapter.getLoaderView());
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
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Pick file"), requestCode);
    }

    private void openTranslateLocale() {
        startActivity(new Intent(context, LocaleTranslateActivity.class));
    }

    private void openEnglishPatcher() {
        LoadAssets.updateEnglishPatch(context, new FutureCallback<DCLocalePatch>() {
            @Override
            public void onCompleted(Exception e, DCLocalePatch patch) {
                new AsyncPatch(context, true).execute(patch);
            }
        });
    }

    private void openRussianPatcher() {
        LoadAssets.updateRussianPatch(context, new FutureCallback<DCLocalePatch>() {
            @Override
            public void onCompleted(Exception e, DCLocalePatch patch) {
                new AsyncPatch(context, true).execute(patch);
            }
        });

    }

    private void openDCWiki() {
        startActivity(new Intent(context, WikiFragmentManagerActivity.class));
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
