package com.arsylk.dcwallpaper.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.arsylk.dcwallpaper.Adapters.LocaleSubfilesAdapter;
import com.arsylk.dcwallpaper.Adapters.SubfileDictAdapter;
import com.arsylk.dcwallpaper.Async.AsyncLoadLocale;
import com.arsylk.dcwallpaper.Async.AsyncWithDialog;
import com.arsylk.dcwallpaper.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.dcwallpaper.Async.interfaces.OnPatchChangedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class LocaleTranslateActivity extends AppCompatActivity implements OnLocaleUnpackFinished {
    private Context context = LocaleTranslateActivity.this;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ListView navigationList;
    private LocaleSubfilesAdapter subfilesAdapter = null;
    private ListView keyvaluesList;
    private SubfileDictAdapter dictAdapter = null;
    private DCLocalePatch original = null, patched = null, patchedNew = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Define.REQUEST_FILE_PATCH && data != null) {
            File file = Utils.uriToFile(data.getData());
            if(file == null)
                return;
            JSONObject patchJson = Utils.fileToJson(file);
            if(patchJson != null) {
                pickedPatchLocale(new DCLocalePatch(patchJson));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_locale_translate);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initViews();
        new AsyncLoadLocale(context, true)
                .setOnLocaleUnpackFinished(this)
                .execute(new File(DCTools.getDCLocalePath()));
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.translate_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_load_locale:
                new AsyncLoadLocale(context, true)
                        .setOnLocaleUnpackFinished(this)
                        .execute(new File(DCTools.getDCLocalePath()), null /*two files to force reload*/);
                return true;
            case R.id.menu_load_patch:
                loadPatchLocale(false);
                return true;
            case R.id.menu_show_patch:
                showAndSaveLocale(patched, "patch");
                return true;
            case R.id.menu_show_patchNew:
                showAndSaveLocale(patchedNew, "patch_new");
                return true;
            case R.id.menu_show_locale:
                showAndSaveLocale(original, "locale");
                return true;
            case R.id.menu_show_patchLocale:
                DCLocalePatch.Subfile[] subfiles = new DCLocalePatch.Subfile[original.getHashFiles().size()];
                original.getHashFiles().values().toArray(subfiles);
                DCLocalePatch copyOriginal = new DCLocalePatch(subfiles);
                if(patched != null) {
                    for(DCLocalePatch.Subfile subfile : patched.getHashFiles().values()) {
                        if(copyOriginal.getHashFile(subfile.getHash()) != null) {
                            for(Map.Entry<String, String> entry : subfile.getDict().entrySet()) {
                                if(copyOriginal.getHashFileDictValue(subfile.getHash(), entry.getKey()) != null) {
                                    copyOriginal.getHashFile(subfile.getHash()).setValue(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                }
                showAndSaveLocale(copyOriginal, "patched_locale");
                return true;
            case R.id.menu_show_upload:
                if(patchedNew != null) {
                    uploadPatchLocale(patchedNew);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinished(DCLocalePatch locale) {
        //assign loaded locale
        original = locale;

        //assign session translations patch
        patched = new DCLocalePatch();
        patchedNew = new DCLocalePatch();

        //setup toolbar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Locale Translate");
        }

        //setup navigation views
        subfilesAdapter.setPickedItem(-1);
        subfilesAdapter.setLocale(original);
        subfilesAdapter.applyPatch(patched);

        if(dictAdapter != null) {
            dictAdapter.setSubfile(null);
        }

        //load community patch
        loadPatchLocale(true);
    }

    private void initViews() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
        }

        drawerLayout = findViewById(R.id.translate_drawer_layout);
        navigationView = findViewById(R.id.translate_navigation_view);

        navigationList = findViewById(R.id.translate_navigation_list);
        subfilesAdapter = new LocaleSubfilesAdapter(context);
        navigationList.setAdapter(subfilesAdapter);
        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //update item
                subfilesAdapter.setPickedItem(position);

                //update toolbar title
                if(getSupportActionBar() != null)
                    getSupportActionBar().setTitle(subfilesAdapter.getItem(position).getHash());

                //update main layout
                dictAdapter.setSubfile(subfilesAdapter.getItem(position));
                dictAdapter.applyPatch(patched);

                //dismiss navigation view
                drawerLayout.closeDrawer(Gravity.START);
            }
        });

        keyvaluesList = findViewById(R.id.translate_list);
        dictAdapter = new SubfileDictAdapter(context);
        dictAdapter.setOnPatchChangedListener(new OnPatchChangedListener() {
            @Override
            public void onPatchChanged(DCLocalePatch patch, DCLocalePatch.Subfile subfile, String key, String val) {
                //update patch
                patched = patch;

                //update adapter
                if(subfilesAdapter != null) {
                    subfilesAdapter.applyPatch(patched);
                }

                if(val != null) {
                    //update patchedNew
                    if(patchedNew.getHashFile(subfile.getHash()) == null) {
                        patchedNew.addSubfile(new DCLocalePatch.Subfile(subfile.getHash(), subfile.getLineType(), new LinkedHashMap<String, String>()));
                    }
                    patchedNew.getHashFile(subfile.getHash()).setValue(key, val);
                }else {
                    //remove from patchedNew
                    if(patchedNew.getHashFile(subfile.getHash()) != null) {
                        patchedNew.getHashFile(subfile.getHash()).delValue(key);
                        if(patchedNew.getHashFileDict(subfile.getHash()).size() == 0) {
                            patchedNew.delSubfile(patchedNew.getHashFile(subfile.getHash()));

                        }
                    }
                }

            }
        });
        keyvaluesList.setAdapter(dictAdapter);

        dictAdapter.setSearchPanel(findViewById(R.id.translate_search_layout));
    }

    //alert dialogs
    private void pickedPatchLocale(final DCLocalePatch patch) {
        //prepare items
        final String[] items = new String[patch.getHashFiles().size()+1];
        final List<DCLocalePatch.Subfile> subfiles = new ArrayList<>(patch.getHashFiles().values());
        items[0] = patch.getName()+" "+patch.getDate();
        for(DCLocalePatch.Subfile subfile : subfiles) {
            items[subfiles.indexOf(subfile)+1] = subfile.getHash()+" <"+subfile.getDict().size()+">";
        }

        //alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("New patch found!");
        ListView listView = new ListView(context);
        listView.setDividerHeight(0);
        listView.setClickable(false);
        listView.setSelector(android.R.color.transparent);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items));
        builder.setView(listView);
        builder.setCancelable(false);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //set patch
                patched = patch;
                patchedNew = new DCLocalePatch();

                //update adapters
                if(subfilesAdapter != null) {
                    subfilesAdapter.applyPatch(patched);
                }
                if(dictAdapter != null) {
                    dictAdapter.applyPatch(patched);
                }
            }
        });
        builder.setNeutralButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //add patch
                if(patched == null) patched = new DCLocalePatch();
                patchedNew = new DCLocalePatch();

                //check for conflicts
                List<LinkedHashMap.Entry<String, String>> conflicts = new ArrayList<>();
                for(DCLocalePatch.Subfile subfile : patch.getHashFiles().values()) {
                    if(patched.getHashFile(subfile.getHash()) == null) {
                        patched.addSubfile(subfile);
                    }else {
                        for(Map.Entry<String, String> entry : subfile.getDict().entrySet()) {
                            String oldVal = patched.getHashFile(subfile.getHash()).getValue(entry.getKey());
                            if(oldVal != null) {
                                if(!oldVal.equals(entry.getValue())) {
                                    conflicts.add(new AbstractMap.SimpleEntry<>(subfile.getHash(), entry.getKey()));
                                }
                            }else {
                                patched.getHashFile(subfile.getHash()).setValue(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }

                //update adapters
                if(subfilesAdapter != null) {
                    subfilesAdapter.applyPatch(patched);
                }
                if(dictAdapter != null) {
                    dictAdapter.applyPatch(patched);
                }

                //resolve conflicts
                if(conflicts.size() > 0) {
                    resolveConflictsDialog(patch, conflicts);
                }
            }
        });
        builder.create().show();
    }

    private void resolveConflictsDialog(final DCLocalePatch patch, final List<Map.Entry<String, String>> conflicts) {
        final boolean[] checked = new boolean[conflicts.size()];
        Arrays.fill(checked, false);
        String[] items = new String[conflicts.size()];
        for(Map.Entry<String, String> entry : conflicts) {
            String currentValue = patched.getHashFileDictValue(entry.getKey(), entry.getValue());
            String newValue = patch.getHashFileDictValue(entry.getKey(), entry.getValue());
            items[conflicts.indexOf(entry)] = String.format("'%s' => '%s'", currentValue, newValue);
        }

        //alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Resolve conflicts!");
        builder.setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Replace Checked", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //patch only checked
                for(Map.Entry<String, String> entry : conflicts) {
                    if(checked[conflicts.indexOf(entry)]) {
                        patched.getHashFile(entry.getKey()).setValue(entry.getValue(), patch.getHashFileDictValue(entry.getKey(), entry.getValue()));
                    }
                }

                //update adapters
                if(subfilesAdapter != null) {
                    subfilesAdapter.applyPatch(patched);
                }
                if(dictAdapter != null) {
                    dictAdapter.applyPatch(patched);
                }
            }
        });
        builder.setNeutralButton("Replace Unchecked", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //patch only checked
                for(Map.Entry<String, String> entry : conflicts) {
                    if(!checked[conflicts.indexOf(entry)]) {
                        patched.getHashFile(entry.getKey()).setValue(entry.getValue(), patch.getHashFileDictValue(entry.getKey(), entry.getValue()));
                    }
                }

                //update adapters
                if(subfilesAdapter != null) {
                    subfilesAdapter.applyPatch(patched);
                }
                if(dictAdapter != null) {
                    dictAdapter.applyPatch(patched);
                }
            }
        });
        builder.create().show();
    }

    private void showAndSaveLocale(final DCLocalePatch patch, final String name) {
        if(patch == null) return;
        final String generated = patch.generate();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ListView listView = new ListView(context);
        listView.setDividerHeight(0);
        listView.setClickable(false);
        listView.setSelector(android.R.color.transparent);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setAdapter(new ArrayAdapter<>(context, R.layout.item_textline, R.id.label, generated.split("\n")));
        builder.setView(listView);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = new File(Define.BASE_DIRECTORY, name+"_"+System.currentTimeMillis()+".json");
                    FileUtils.write(file, generated, Charset.forName("utf-8"));
                    Toast.makeText(context, "Saved to: "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.create().show();

    }

    //load from server
    private void loadPatchLocale(final boolean force) {
        new AsyncWithDialog<Void, Void, DCLocalePatch>(context, true, "Downloading...") {
            @Override
            protected DCLocalePatch doInBackground(Void... voids) {
                try {
                    String raw = Ion.with(context).load(Define.REMOTE_ASSET_COMMUNITY_PATCH)
                            .noCache()
                            .asString(Charset.forName("utf-8")).get();
                    return new DCLocalePatch(new JSONObject(raw));
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(DCLocalePatch patch) {
                super.onPostExecute(patch);
                if(force) {
                    //set patch
                    patched = patch;
                    patchedNew = new DCLocalePatch();

                    //update adapters
                    if(subfilesAdapter != null) {
                        subfilesAdapter.applyPatch(patched);
                    }
                    if(dictAdapter != null) {
                        dictAdapter.applyPatch(patched);
                    }
                }else {
                    pickedPatchLocale(patch);
                }
            }
        }.execute();
    }

    //upload to server
    private void uploadPatchLocale(final DCLocalePatch patch) {
        new AsyncWithDialog<Void, Long, Boolean>(context, true, "Uploading...") {
            @Override
            protected void onProgressUpdate(Long... values) {
                if(values.length > 1) {
                    dialog.setMessage("Uploading: "+(values[0].floatValue()/values[1].floatValue())*100+"%");
                }
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    String rwa = Ion.with(context).load(Define.UPLOAD_COMMUNITY_PATCH)
                            .uploadProgress(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    publishProgress(uploaded, total);
                                }
                            })
                            .setBodyParameter("json", patch.generate())
                            .asString().get();
                    System.out.println(rwa);
                    return true;
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean successful) {
                super.onPostExecute(successful);
                Toast.makeText(context, successful ? "Upload finished!" : "Upload failed!", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
