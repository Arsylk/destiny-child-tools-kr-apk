package com.arsylk.mammonsmite.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.arsylk.mammonsmite.Adapters.LocaleSubfilesAdapter;
import com.arsylk.mammonsmite.Adapters.SubfileDictAdapter;
import com.arsylk.mammonsmite.Async.AsyncLoadLocale;
import com.arsylk.mammonsmite.Async.AsyncWithDialog;
import com.arsylk.mammonsmite.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.mammonsmite.Async.interfaces.OnPatchChangedListener;
import com.arsylk.mammonsmite.DestinyChild.DCLocalePatch;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Utils;
import com.arsylk.mammonsmite.views.BigTextDialog;
import com.arsylk.mammonsmite.views.InputTextDialog;
import com.arsylk.mammonsmite.views.PickWhichDialog;
import com.arsylk.mammonsmite.views.ResolveConflictsDialog;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.File;
import java.util.*;

public class LocaleTranslateActivity extends ActivityWithExceptionRedirect implements OnLocaleUnpackFinished {
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
                loadPatch(new DCLocalePatch(patchJson));
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
                .execute(DCTools.getDCLocalePath());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toast.makeText(context, "Orientation: "+newConfig.orientation, Toast.LENGTH_SHORT).show();
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
            case R.id.menu_add_key:
                showAddKey();
                return true;
            case R.id.menu_load_patch:
                showLoadPatches();
                return true;
            case R.id.menu_show_patches:
                showPickPatches(new PickWhichDialog.Option.OnOptionPicked<DCLocalePatch>() {
                    @Override
                    public void onOptionPicked(PickWhichDialog.Option<DCLocalePatch> option) {
                        if(option != null) {
                            new BigTextDialog(context, option.getLabel(), option.getObject().generate()).show();
                        }
                    }
                });
                return true;
            case R.id.menu_apply_patch:
                showApplyPatches();
                return true;
            case R.id.menu_show_upload:
                showPickPatches(new PickWhichDialog.Option.OnOptionPicked<DCLocalePatch>() {
                    @Override
                    public void onOptionPicked(PickWhichDialog.Option<DCLocalePatch> option) {
                        if(option != null) {
                            //upload picked patch
                            uploadPatchLocale(option.getObject());
                        }
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    //fill views
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
                    getSupportActionBar().setTitle(subfilesAdapter.getItem(position).getHash().toUpperCase());

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

        subfilesAdapter.setSearchPanel(findViewById(R.id.translate_search_layout));
        dictAdapter.setSearchPanel(findViewById(R.id.translate_search_layout));
    }

    @Override
    public void onFinished(DCLocalePatch locale) {
        //check if not null
        if(locale == null) {
            Toast.makeText(context, "Failed to load locale!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //assign loaded locale
        original = locale;

        //assign session translations patch
        patched = new DCLocalePatch();
        patchedNew = new DCLocalePatch();

        //setup navigation views
        subfilesAdapter.setPickedItem(0);
        subfilesAdapter.setLocale(original);
        subfilesAdapter.applyPatch(patched);

        //setup toolbar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subfilesAdapter.getItem(0).getHash().toUpperCase());
        }

        //setup dict list view
        if(dictAdapter != null) {
            dictAdapter.setSubfile(subfilesAdapter.getItem(0));
        }
    }

    //options menu choices
    private void showAddKey() {
        new InputTextDialog(context, "Add Key", "Key", new InputTextDialog.OnInputSubmitted() {
            @Override
            public void onInputSubmitted(final String key) {
                if(!key.isEmpty()) {
                    new InputTextDialog(context, "Add Key", "Value", new InputTextDialog.OnInputSubmitted() {
                        @Override
                        public void onInputSubmitted(String val) {
                            dictAdapter.updatePatch(key, val);
                            DCLocalePatch.Subfile subfile = dictAdapter.getSubfile();
                            subfile.setValue(key, "");
                            dictAdapter.setSubfile(subfile);
                        }
                    }).show();
                }
            }
        }).show();

    }

    private void showLoadPatches() {
        //create options
        List<PickWhichDialog.Option<Integer>> options = new ArrayList<>();
        options.add(new PickWhichDialog.Option<Integer>("Load from server", 0));
        options.add(new PickWhichDialog.Option<Integer>("Load from device", 1));

        //show option pick dialog
        new PickWhichDialog<Integer>(context, options)
            .setOnOptionPicked(new PickWhichDialog.Option.OnOptionPicked<Integer>() {
                @Override
                public void onOptionPicked(PickWhichDialog.Option<Integer> option) {
                    if(option != null) {
                        if(option.getObject() == 0) {
                            //load patch from server
                            loadPatchLocale();
                        }else if(option.getObject() == 1) {
                            //load patch from internal storage
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("*/*");
                            startActivityForResult(Intent.createChooser(intent, "Pick file"), Define.REQUEST_FILE_PATCH);
                        }
                    }
                }
        }).show();
    }

    private void showPickPatches(PickWhichDialog.Option.OnOptionPicked<DCLocalePatch> onOptionPicked) {
        //create options
        List<PickWhichDialog.Option<DCLocalePatch>> options = new ArrayList<>();
        options.add(new PickWhichDialog.Option<DCLocalePatch>("patch full", patched));
        options.add(new PickWhichDialog.Option<DCLocalePatch>("patch new", patchedNew));
        options.add(new PickWhichDialog.Option<DCLocalePatch>("locale original", original));
        options.add(new PickWhichDialog.Option<DCLocalePatch>("locale patched", DCLocalePatch.clone(original).patch(patched)));

        //show option pick dialog
        new PickWhichDialog<DCLocalePatch>(context, options)
            .setOnOptionPicked(onOptionPicked).show();
    }

    private void showApplyPatches() {
        //create options
        List<PickWhichDialog.Option<DCLocalePatch>> options = new ArrayList<>();
        options.add(new PickWhichDialog.Option<DCLocalePatch>("patch full", patched));
        options.add(new PickWhichDialog.Option<DCLocalePatch>("patch new", patchedNew));

        //show option pick dialog
        new PickWhichDialog<>(context, options)
            .setOnOptionPicked(new PickWhichDialog.Option.OnOptionPicked<DCLocalePatch>() {
                @Override
                public void onOptionPicked(PickWhichDialog.Option<DCLocalePatch> option) {
                    if(option != null) {
                        //patch locale task
                        new AsyncWithDialog<DCLocalePatch, Void, Boolean>(context, true, "Patching locale...") {
                            @Override
                            protected Boolean doInBackground(DCLocalePatch... dcLocalePatches) {
                                try {
                                    //patch locale
                                    DCTools.patchLocale(DCTools.getDCLocalePath(), dcLocalePatches[0], context.get());

                                    //patch loaded locale
                                    original.patch(dcLocalePatches[0]);

                                    return true;
                                }catch(Exception e) {
                                    e.printStackTrace();
                                }

                                return false;
                            }
                        }.setOnPostExecute(new Utils.OnPostExecute<Boolean>() {
                            @Override
                            public void onPostExecute(Boolean result) {
                                Toast.makeText(context, result ? "Successfully patched locale!" : "Something went wrong!", Toast.LENGTH_SHORT).show();
                                dictAdapter.notifyDataSetChanged();
                            }
                        }).execute(option.getObject());
                    }
                }
        }).show();

    }

    private void loadPatch(final DCLocalePatch patch) {
        new BigTextDialog(context, "New patch found!", patch.generate())
                .setPositiveButton("Set patch", new DialogInterface.OnClickListener() {
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
                })
                .setNeutralButton("Add patch", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //add patch
                        //check for conflicts
                        List<ResolveConflictsDialog.Conflict> conflicts = new ArrayList<>();
                        for(DCLocalePatch.Subfile subfile : patch.getHashFiles().values()) {
                            if(patched.getHashFile(subfile.getHash()) == null) {
                                patched.addSubfile(subfile);
                            }else {
                                for(Map.Entry<String, String> entry : subfile.getDict().entrySet()) {
                                    String oldVal = patched.getHashFile(subfile.getHash()).getValue(entry.getKey());
                                    if(oldVal != null) {
                                        if(!oldVal.equals(entry.getValue())) {
                                            conflicts.add(new ResolveConflictsDialog.Conflict(subfile.getHash(), entry.getKey(), oldVal, entry.getValue()));
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
                            new ResolveConflictsDialog(context, conflicts).setOnConflictResolved(new ResolveConflictsDialog.Conflict.OnConflictResolved() {
                                @Override
                                public void onConflictResolved(ResolveConflictsDialog.Conflict conflict) {
                                    //update patch
                                    patched.getHashFile(conflict.getHash()).setValue(conflict.getKey(), conflict.resolve());

                                    //update adapter
                                    if(dictAdapter != null) {
                                        dictAdapter.applyPatch(patched);
                                    }
                                }
                            }).show();
                        }
                    }
                })
        .show();
    }


    //server methods
    private void loadPatchLocale() {
        new AsyncWithDialog<Void, Void, DCLocalePatch>(context, true, "Downloading...") {
            @Override
            protected DCLocalePatch doInBackground(Void... voids) {
                try {
                    String raw = Jsoup.connect(Define.REMOTE_ASSET_COMMUNITY_PATCH)
                            .followRedirects(true).ignoreContentType(true)
                            .get().body().text();
                    return new DCLocalePatch(new JSONObject(raw));
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final DCLocalePatch patch) {
                super.onPostExecute(patch);
                loadPatch(patch);
            }
        }.execute();
    }

    private void uploadPatchLocale(final DCLocalePatch patch) {
        //password dialog
        new InputTextDialog(context, "Input", "Password", new InputTextDialog.OnInputSubmitted() {
            @Override
            public void onInputSubmitted(final String text) {
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
                            String response = Ion.with(context.get()).load(Define.UPLOAD_COMMUNITY_PATCH)
                                    .uploadProgress(new ProgressCallback() {
                                        @Override
                                        public void onProgress(long uploaded, long total) {
                                            publishProgress(uploaded, total);
                                        }
                                    })
                                    .setBodyParameter("key", text)
                                    .setBodyParameter("json", patch.generate())
                                    .asString().get();
                            Log.d("mTag:Upload", response);
                            return true;
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean successful) {
                        super.onPostExecute(successful);
                        Toast.makeText(context.get(), successful ? "Upload finished!" : "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
        }).show();
    }
}
