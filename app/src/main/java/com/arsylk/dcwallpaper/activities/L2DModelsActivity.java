package com.arsylk.dcwallpaper.activities;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.L2DModelItem;
import com.arsylk.dcwallpaper.Adapters.L2DModelsAdapter;
import com.arsylk.dcwallpaper.Async.interfaces.OnPackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.Live2D.LiveWallpaperService;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.views.BigTextDialog;
import com.arsylk.dcwallpaper.views.PickWhichDialog;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class L2DModelsActivity extends ActivityWithExceptionRedirect {
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
                final List<L2DModelItem> items = new ArrayList<>();
                for(File dir : Define.MODELS_DIRECTORY.listFiles()) {
                    File _model = new File(dir, "_model");
                    if(_model.exists()) {
                        L2DModelItem item = new L2DModelItem(_model);
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

    private void handlePickItem(final L2DModelItem item) {
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

    //action picker dialog
    public static void showPickAction(final Context context, final L2DModel l2DModel, final FutureCallback<Integer> callback) {
        //create options
        List<PickWhichDialog.Option<Integer>> options = new ArrayList<>();
        options.add(new PickWhichDialog.Option<Integer>("Preview", 0));
        options.add(new PickWhichDialog.Option<Integer>("Load", 1));
        options.add(new PickWhichDialog.Option<Integer>("Restore", 2));
        options.add(new PickWhichDialog.Option<Integer>("Delete", 3));
        options.add(new PickWhichDialog.Option<Integer>("Wallpaper", 4));
        options.add(new PickWhichDialog.Option<Integer>("Info", 5));
        options.add(new PickWhichDialog.Option<Integer>("Open", 6));


        //create dialog
        PickWhichDialog<Integer> pickAction = new PickWhichDialog<>(context, options);
        pickAction.setTitle("Pick Action");
        pickAction.setOnOptionPicked(new PickWhichDialog.Option.OnOptionPicked<Integer>() {
            @Override
            public void onOptionPicked(PickWhichDialog.Option<Integer> option) {
                //if null
                if(option == null) return;

                switch(option.getObject()) {
                    //start preview activty
                    case 0:
                        actionPreview(context, l2DModel);
                        break;
                    //pack and load to game
                    case 1:
                        actionLoad(context, l2DModel);
                        break;
                    //load backup to game
                    case 2:
                        actionRestore(context, l2DModel);
                        break;
                    //remove saved model
                    case 3:
                        actionDelete(l2DModel.getOutput());
                        break;
                    //set as wallpaper
                    case 4:
                        actionWallpaper(context, l2DModel);
                        break;
                    //show model info
                    case 5:
                        actionInfo(context, l2DModel);
                        break;
                    //open unpack folder
                    case 6:
                        actionOpen(context, l2DModel);
                        break;
                }

                //notify action picked
                if(callback != null) {
                    callback.onCompleted(null, option.getObject());
                }
            }
        });
        pickAction.show();
        //
//        if(true) return;
//
//
//
//        //TODO old way trying new
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Choose action:");
//        builder.setItems(new String[]{"Preview", "Load", "Restore", "Delete", "Wallpaper", "Info"}, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                switch(i) {
//                    case 0:
//                        //start preview activity
//                        PreferenceManager.getDefaultSharedPreferences(context).edit()
//                                .putString("model_id", l2DModel.getModelId())
//                                .putString("preview_model", l2DModel.getModel().getAbsolutePath())
//                                .commit();
//                        context.startActivity(new Intent(context, L2DPreviewActivity.class).putExtra("mode", L2DConfig.MODE_PREVIEW));
//                        break;
//                    case 1:
//                        //pack and fileLoad model to game
//                        File packTo = new File(l2DModel.getOutput(), l2DModel.getModelId()+".pck");
//                        DCTools.asyncPack(l2DModel.getOutput(), packTo, context, new OnPackFinishedListener() {
//                            @Override
//                            public void onFinished(File file) {
//                                if(file == null || !file.exists() || file.isDirectory() || !file.canRead()) {
//                                    return;
//                                }
//                                Toast.makeText(context, "Packed to: "+file.getName(), Toast.LENGTH_SHORT).show();
//                                try {
//                                    //backup game pck file
//                                    File dcPckFile = new File(DCTools.getDCModelsPath(), file.getName());
//                                    File bakPckFile = new File(file.getParentFile(), "_"+file.getName()+".bak");
//                                    if(dcPckFile.exists()) {
//                                        if(!bakPckFile.exists()) {
//                                            Toast.makeText(context, "Backup to: "+bakPckFile.getName(), Toast.LENGTH_SHORT).show();
//                                            FileUtils.copyFile(dcPckFile, bakPckFile);
//                                        }
//                                        FileUtils.deleteQuietly(dcPckFile);
//                                        FileUtils.moveFile(file, dcPckFile);
//                                        Toast.makeText(context, "Loaded model: "+dcPckFile.getName(), Toast.LENGTH_SHORT).show();
//                                    }else {
//                                        Toast.makeText(context, "Failed to find DC model!", Toast.LENGTH_SHORT).show();
//                                    }
//                                }catch(Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        break;
//                    case 2:
//                        //restore backup model to game
//                        try {
//                            File bakPckFile = new File(l2DModel.getOutput(), "_"+l2DModel.getModelId()+".pck.bak");
//                            if(bakPckFile.exists()) {
//                                File dcPckFile = new File(DCTools.getDCModelsPath(), l2DModel.getModelId()+".pck");
//                                if(dcPckFile.exists()) {
//                                    FileUtils.deleteQuietly(dcPckFile);
//                                }
//                                FileUtils.copyFile(bakPckFile, dcPckFile);
//                                Toast.makeText(context, "Restored model: "+dcPckFile.getName(), Toast.LENGTH_SHORT).show();
//                            }else {
//                                Toast.makeText(context, "No backup file found!", Toast.LENGTH_SHORT).show();
//                            }
//                        }catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case 3:
//                        //remove model files
//                        try {
//                            for(File file : l2DModel.getOutput().listFiles()) {
//                                FileUtils.deleteQuietly(file);
//                            }
//                            l2DModel.getOutput().delete();
//                        }catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case 4:
//                        //set as wallpaper
//                        new L2DConfig(context, L2DConfig.MODE_WALLPAPER, l2DModel.getModel().getAbsolutePath()).writeToPrefs(context);
//                        try {
//                            //check if wallpaper is already set
//                            boolean isSet = false;
//                            try {
//                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
//                                if(wallpaperManager != null) {
//                                    if(wallpaperManager.getWallpaperInfo() != null) {
//                                        ComponentName componentName = wallpaperManager.getWallpaperInfo().getComponent();
//                                        if(componentName != null) {
//                                            if(componentName.equals(new ComponentName(context, LiveWallpaperService.class))) {
//                                                isSet = true;
//                                            }
//                                        }
//                                    }
//                                }
//                            }catch(Exception e) {
//                                e.printStackTrace();
//                                isSet = false;
//                            }
//                            if(!isSet) {
//                                //pick wallpaper intent
//                                Intent intent;
//                                if(Build.VERSION.SDK_INT >= 16) {
//                                    intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//                                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                                            new ComponentName(context, LiveWallpaperService.class));
//                                }else {
//                                    intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
//                                }
//                                if(context instanceof Activity)
//                                    ((Activity) context).startActivityForResult(intent, Define.REQUEST_WALLPAPER_SET);
//                                else
//                                    context.startActivity(intent);
//                            }else {
//                                //request wallpaper reload
//                                if(LiveWallpaperService.getInstance() != null) {
//                                    LiveWallpaperService.getInstance().requestReload();
//                                }
//                                Toast.makeText(context, "Wallpaper updated!", Toast.LENGTH_SHORT).show();
//                            }
//                        }catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    case 5:
//                        //show model info
//                        try {
//                            BigTextDialog infoDialog = new BigTextDialog(context, "model_info.json", l2DModel.getModelInfoJson().toString(4));
//                            infoDialog.setPositiveButton("Load", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    try {
//                                        DCTools.asyncApplyModelInfo(l2DModel, context);
//                                    }catch(Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                            infoDialog.show();
//                        }catch(Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        break;
//                }
//                if(callback != null) callback.onCompleted(null, i);
//            }
//        });
//        builder.create().show();
    }

    //all actions
    public static void actionPreview(Context context, L2DModel l2DModel) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("model_id", l2DModel.getModelId())
                .putString("preview_model", l2DModel.getModel().getAbsolutePath())
                .commit();
        context.startActivity(new Intent(context, L2DPreviewActivity.class).putExtra("mode", L2DConfig.MODE_PREVIEW));
    }

    public static void actionLoad(final Context context, L2DModel l2DModel) {
        File packTo = new File(l2DModel.getOutput(), l2DModel.getModelId()+".pck");
        DCTools.asyncPack(l2DModel.getOutput(), packTo, context, new OnPackFinishedListener() {
            @Override
            public void onFinished(File file) {
                if(file == null || !file.exists() || file.isDirectory() || !file.canRead()) {
                    return;
                }
                Toast.makeText(context, "Packed to: "+file.getName(), Toast.LENGTH_SHORT).show();
                try {
                    //backup game pck file
                    File dcPckFile = new File(DCTools.getDCModelsPath(), file.getName());
                    File bakPckFile = new File(file.getParentFile(), "_"+file.getName()+".bak");
                    if(dcPckFile.exists()) {
                        if(!bakPckFile.exists()) {
                            Toast.makeText(context, "Backup to: "+bakPckFile.getName(), Toast.LENGTH_SHORT).show();
                            FileUtils.copyFile(dcPckFile, bakPckFile);
                        }else {
                            Toast.makeText(context, "Backup already exists", Toast.LENGTH_SHORT).show();
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
    }

    public static void actionRestore(Context context, L2DModel l2DModel) {
        try {
            File bakPckFile = new File(l2DModel.getOutput(), "_"+l2DModel.getModelId()+".pck.bak");
            if(bakPckFile.exists()) {
                File dcPckFile = new File(DCTools.getDCModelsPath(), l2DModel.getModelId()+".pck");
                if(dcPckFile.exists()) {
                    FileUtils.deleteQuietly(dcPckFile);
                }
                FileUtils.copyFile(bakPckFile, dcPckFile);
                Toast.makeText(context, "Restored model: "+dcPckFile.getName(), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "No backup file found!", Toast.LENGTH_SHORT).show();
            }
            if(l2DModel.getModelInfoBakJson().length() > 0) {
                DCTools.asyncApplyModelInfo(l2DModel, context, true);
                Toast.makeText(context, "Restored model info!", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void actionDelete(File output) {
        try {
            for(File file : output.listFiles()) {
                FileUtils.deleteQuietly(file);
            }
            output.delete();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void actionWallpaper(Context context, L2DModel l2DModel) {
        //write new config to shared prefs
        new L2DConfig(context, L2DConfig.MODE_WALLPAPER, l2DModel.getModel().getAbsolutePath()).writeToPrefs(context);
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
    }

    public static void actionInfo(final Context context, final L2DModel l2DModel) {
        try {
            BigTextDialog infoDialog = new BigTextDialog(context, "_model", l2DModel.getModelConfigJson().toString(4));
            infoDialog.setPositiveButton("Load", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        DCTools.asyncApplyModelInfo(l2DModel, context, false);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            infoDialog.show();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void actionOpen(Context context, L2DModel l2DModel) {
        actionOpen(context, l2DModel.getOutput());
    }

    public static void actionOpen(Context context, File folder) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(folder.getAbsolutePath()), "resource/folder");
        if(intent.resolveActivityInfo(context.getPackageManager(), 0) != null) {
            context.startActivity(Intent.createChooser(intent, "Open Folder"));
        }else {
            Toast.makeText(context, "No file explorer found!", Toast.LENGTH_SHORT).show();
        }
    }
}
