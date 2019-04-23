package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.arsylk.dcwallpaper.Adapters.L2DModelItem;
import com.arsylk.dcwallpaper.Adapters.L2DModelsAdapter;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.views.DCSwapFileView;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.arsylk.dcwallpaper.utils.Define.MODELS_DIRECTORY;

public class DCSwapActivity extends AppCompatActivity {
    private interface OnModelItemPick {
        void onModelItemPick(L2DModelItem item);
    }
    private Context context = DCSwapActivity.this;
    private View fromModelView, toModelView;
    private L2DModelItem fromModelItem, toModelItem;
    private DragLinearLayout fromDragView, toDragView;
    private List<DCSwapFileView> fromFileViews, toFileViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcswap);
        initViews();
    }

    private void initViews() {
        ScrollView scrollView = findViewById(R.id.scroll_layout);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        fromDragView = findViewById(R.id.drag_list_from);
        toDragView = findViewById(R.id.drag_list_to);
        fromDragView.setContainerScrollView(scrollView);
        toDragView.setContainerScrollView(scrollView);

        findViewById(R.id.swap_try).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                L2DModelItem temp;
                temp = fromModelItem;
                fromModelItem = toModelItem;
                toModelItem = temp;
                if(fromModelItem != null) {
                    fillModelView(fromModelItem, fromModelView);
                    fillModelFilesView(fromModelItem, fromDragView);
                }
                if(toModelItem != null) {
                    fillModelView(toModelItem, toModelView);
                    fillModelFilesView(toModelItem, toDragView);
                }
                return true;
            }
        });

        fromModelView = findViewById(R.id.item_from);
        toModelView = findViewById(R.id.item_to);
        View.OnClickListener onModelViewClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.item_from:
                        showPickModel(new OnModelItemPick() {
                            @Override
                            public void onModelItemPick(L2DModelItem item) {
                                fillModelView(item, fromModelView);
                                fillModelFilesView(item, fromDragView);
                                fromModelItem = item;
                            }
                        });
                        break;
                    case R.id.item_to:
                        showPickModel(new OnModelItemPick() {
                            @Override
                            public void onModelItemPick(L2DModelItem item) {
                                fillModelView(item, toModelView);
                                fillModelFilesView(item, toDragView);
                                toModelItem = item;
                            }
                        });
                        break;
                }
            }
        };
        fromModelView.setOnClickListener(onModelViewClick);
        toModelView.setOnClickListener(onModelViewClick);

        findViewById(R.id.swap_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toModelItem != null && fromModelItem != null) {
                    try {
                        swapModels();
                        Toast.makeText(context, "Swap successful!", Toast.LENGTH_SHORT).show();
                    }catch(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(context, "Pick models first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillModelView(L2DModelItem item, View view) {
        TextView label = view.findViewById(R.id.label);
        TextView sublabel = view.findViewById(R.id.sub_label);
        ImageView imglabel = view.findViewById(R.id.img_label);

        label.setText(item.getModelName());
        sublabel.setText(item.getModelId());
        if(item.getPreview() != null) {
            imglabel.setImageBitmap(item.getPreview());
        }

    }

    private void fillModelFilesView(L2DModelItem item, DragLinearLayout layout) {
        //list files for model
        List<File> filenames = new ArrayList<>();
        for(File file : item.getFile().listFiles()) {
            if(!file.getName().startsWith("_"))
                filenames.add(file);
        }

        //clear child views
        layout.removeAllViewsInLayout();
        layout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                updateColorLines();
            }
        });

        //add new child views
        for(File filename : filenames) {
            DCSwapFileView view = new DCSwapFileView(context, filename, layout.getId() == R.id.drag_list_from);
            layout.addDragView(view, view);
        }

        //draw lines
        updateColorLines();
    }

    private void updateColorLines() {
        for(int i = 0; i < Math.max(fromDragView.getChildCount(), toDragView.getChildCount()); i++) {
            if(fromDragView.getChildAt(i) != null && toDragView.getChildAt(i) != null) {
                fromDragView.getChildAt(i).findViewById(R.id.line).setBackgroundColor(Color.GREEN);
                toDragView.getChildAt(i).findViewById(R.id.line).setBackgroundColor(Color.RED);
            }else if(fromDragView.getChildAt(i) != null) {
                fromDragView.getChildAt(i).findViewById(R.id.line).setBackgroundColor(Color.GRAY);
            }else if(toDragView.getChildAt(i) != null) {
                toDragView.getChildAt(i).findViewById(R.id.line).setBackgroundColor(Color.GREEN);
            }
        }
    }

    //methods
    private void swapModels() throws Exception {
        //create swap output directory
        File swapOutput = new File(MODELS_DIRECTORY, toModelItem.getFile().getName()+"_swap");
        int tries = 0;
        while(swapOutput.exists()) {
            tries += 1;
            swapOutput = new File(MODELS_DIRECTORY, toModelItem.getFile().getName()+"_swap_"+tries);
        }
        FileUtils.forceMkdir(swapOutput);
        File fromOutput = fromModelItem.getFile(), toOutput = toModelItem.getFile();
        File from_header = new File(fromOutput, "_header"), to_header = new File(toOutput, "_header");
        if(!from_header.exists() || !to_header.exists()) {
            throw new Exception("Header files are missing");
        }else {
            FileUtils.copyFileToDirectory(to_header, swapOutput);
        }

        //copy selected files
        for(int i = 0; i < toDragView.getChildCount(); i++) {
            File srcFile = (File) toDragView.getChildAt(i).getTag();
            File swapFile = selectItem(i);
            if(srcFile != null && swapFile != null) {
                FileUtils.copyFile(swapFile, new File(swapOutput, srcFile.getName()));
            }
        }


        //update _model & model.json
        String fromId, toId;
        try {
            FileOutputStream fos = new FileOutputStream(new File(swapOutput, "_model"));
            JSONObject toModelJson = Utils.fileToJson(new File(toOutput, "_model"));
            fromId = Utils.fileToJson(new File(fromOutput, "_model")).getString("model_id");
            toId = toModelJson.getString("model_id");
            toModelJson.put("model_name", toModelJson.getString("model_name").trim()+" Swap");
            fos.write(toModelJson.toString(4).getBytes());
            fos.flush();
            fos.close();

            fos = new FileOutputStream(new File(swapOutput, "model.json"), false);
            JSONObject modelJson = Utils.fileToJson(new File(toOutput, "model.json"));
            fos.write(modelJson.toString(4).replaceAll(fromId, toId).getBytes());
            fos.flush();
            fos.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

        //copy preview
        FileUtils.copyFileToDirectory(new File(fromOutput, "_preview.png"), swapOutput);
    }

    private File selectItem(int i) {
        if(fromDragView.getChildAt(i) != null)
            return (File) fromDragView.getChildAt(i).getTag();
        if(toDragView.getChildAt(i) != null)
            return (File) toDragView.getChildAt(i).getTag();
        return null;
    }

    private void showPickModel(final OnModelItemPick onModelItemPick) {
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
                        final L2DModelsAdapter adapter = new L2DModelsAdapter(context, items);
                        ListView listView = new ListView(context);
                        listView.setAdapter(adapter);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(listView);
                        final AlertDialog dialog = builder.create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                if(onModelItemPick != null) {
                                    onModelItemPick.onModelItemPick(adapter.getItem(i));
                                }
                                dialog.dismiss();
                            }
                        });
                        dialog.show();

                    }
                });
            }
        }).start();
    }
}
