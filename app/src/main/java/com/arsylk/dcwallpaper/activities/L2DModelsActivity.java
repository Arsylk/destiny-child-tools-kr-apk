package com.arsylk.dcwallpaper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.DCModelItem;
import com.arsylk.dcwallpaper.Adapters.DCModelsAdapter;
import com.arsylk.dcwallpaper.Adapters.L2DModelsAdapter;
import com.arsylk.dcwallpaper.Async.OnPackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.Live2D.LiveWallpaperService;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class L2DModelsActivity extends AppCompatActivity {
    private Context context = L2DModelsActivity.this;
    private ListView model_list;
    private L2DModelsAdapter adapter = null;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(adapter != null) {
            for(int i = 0; i < adapter.getCount(); i++) {
                adapter.getItem(i).loadPreview();
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l2d_models);
        initViews();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<L2DModelsAdapter.L2DModelItem> items = new ArrayList<>();
                for(File dir : Define.MODELS_DIRECTORY.listFiles()) {
                    File _model = new File(dir, "_model");
                    if(_model.exists()) {
                        L2DModelsAdapter.L2DModelItem item = new L2DModelsAdapter.L2DModelItem(_model);
                        if(item.isLoaded()) {
                            items.add(item);
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new L2DModelsAdapter(context, items);
                        model_list.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    private void initViews() {
        model_list = findViewById(R.id.model_list);
        model_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapter != null) {
                    handlePickItem(adapter.getItem(i));
                }
            }
        });
    }

    private void handlePickItem(final L2DModelsAdapter.L2DModelItem item) {
        L2DModel l2dModel = new L2DModel(item.getFile());
        if(l2dModel.isLoaded()) {
            L2DModelsActivity.showPickAction(context, l2dModel, new FutureCallback<Integer>() {
                @Override
                public void onCompleted(Exception e, Integer result) {
                    if(e == null) {
                        if(result == 3) {
                            adapter.removeItem(item);
                        }
                        adapter.notifyDataSetChanged();
                    }else {
                        e.printStackTrace();
                    }
                }
            });
        }else {
            Toast.makeText(context, "Failed to fileLoad!", Toast.LENGTH_SHORT).show();
            try {
                for(File file : item.getFile().listFiles()) {
                    FileUtils.deleteQuietly(file);
                }
                item.getFile().delete();
            }catch(Exception e) {
                e.printStackTrace();
            }
            adapter.removeItem(item);
            adapter.notifyDataSetChanged();
        }

    }

    public static void showPickAction(Context context, L2DModel l2DModel) {
        showPickAction(context, l2DModel, null);
    }

    public static void showPickAction(final Context context, final L2DModel l2dModel, final FutureCallback<Integer> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose action:");
        builder.setItems(new String[]{"Preview", "Load", "Restore", "Delete", "Wallpaper"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        //start preview activity
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString("model_id", l2dModel.getModelId())
                                .putString("preview_model", l2dModel.getModel().getAbsolutePath())
                                .commit();
                        context.startActivity(new Intent(context, L2DPreviewActivity.class).putExtra("mode", L2DConfig.MODE_PREVIEW));
                        break;
                    case 1:
                        //pack and fileLoad model to game
                        File packTo = new File(l2dModel.getOutput(), l2dModel.getModelId()+".pck");
                        DCTools.asyncPack(l2dModel.getOutput(), packTo, context, new OnPackFinishedListener() {
                            @Override
                            public void onFinished(File file) {
                                if(file == null || !file.exists() || file.isDirectory() || !file.canRead()) {
                                    return;
                                }
                                Toast.makeText(context, "Packed to: "+file.getName(), Toast.LENGTH_SHORT).show();
                                try {
                                    File dcPckFile = new File(DCTools.getDCModelsPath(), file.getName());
                                    File bakPckFile = new File(file.getParentFile(), "_"+file.getName()+".bak");
                                    if(dcPckFile.exists()) {
                                        if(!bakPckFile.exists()) {
                                            Toast.makeText(context, "Backup to: "+bakPckFile.getName(), Toast.LENGTH_SHORT).show();
                                            FileUtils.copyFile(dcPckFile, bakPckFile);
                                        }
                                        FileUtils.deleteQuietly(dcPckFile);
                                        FileUtils.moveFile(file, dcPckFile);
                                        Toast.makeText(context, "Loaded model: "+dcPckFile.getName(), Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(context, "Failed to find DC model!", Toast.LENGTH_SHORT).show();
                                    }
                                }catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case 2:
                        //restore backup model to game
                        try {
                            File bakPckFile = new File(l2dModel.getOutput(), "_"+l2dModel.getModelId()+".pck.bak");
                            if(bakPckFile.exists()) {
                                File dcPckFile = new File(DCTools.getDCModelsPath(), l2dModel.getModelId()+".pck");
                                if(dcPckFile.exists()) {
                                    FileUtils.deleteQuietly(dcPckFile);
                                }
                                FileUtils.copyFile(bakPckFile, dcPckFile);
                                Toast.makeText(context, "Restored model: "+dcPckFile.getName(), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "No backup file found!", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        //remove model files
                        try {
                            for(File file : l2dModel.getOutput().listFiles()) {
                                FileUtils.deleteQuietly(file);
                            }
                            l2dModel.getOutput().delete();
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        //set as wallpaper
                        new L2DConfig(context, L2DConfig.MODE_WALLPAPER, l2dModel.getModel().getAbsolutePath()).writeToPrefs(context);
                        try {
                            //check if wallpaper is already set
                            boolean isSet = false;
                            try {
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                                if(wallpaperManager != null) {
                                    if(wallpaperManager.getWallpaperInfo() != null) {
                                        ComponentName componentName = wallpaperManager.getWallpaperInfo().getComponent();
                                        if(componentName != null) {
                                            if(componentName.equals(new ComponentName(context, LiveWallpaperService.class))) {
                                                isSet = true;
                                            }
                                        }
                                    }
                                }
                            }catch(Exception e) {
                                e.printStackTrace();
                                isSet = false;
                            }
                            if(!isSet) {
                                //pick wallpaper intent
                                Intent intent;
                                if(Build.VERSION.SDK_INT >= 16) {
                                    intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                            new ComponentName(context, LiveWallpaperService.class));
                                }else {
                                    intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                                }
                                if(context instanceof Activity)
                                    ((Activity) context).startActivityForResult(intent, Define.REQUEST_WALLPAPER_SET);
                                else
                                    context.startActivity(intent);
                            }else {
                                //request wallpaper reload
                                if(LiveWallpaperService.getInstance() != null) {
                                    LiveWallpaperService.getInstance().requestReload();
                                }
                                Toast.makeText(context, "Wallpaper updated!", Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                if(callback != null) callback.onCompleted(null, i);
            }
        });
        builder.create().show();
    }
}
