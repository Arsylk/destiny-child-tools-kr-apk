package com.arsylk.mammonsmite.activities;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.arsylk.mammonsmite.Adapters.L2DModelItem;
import com.arsylk.mammonsmite.Adapters.L2DModelsAdapter;
import com.arsylk.mammonsmite.DestinyChild.DCSwapper;
import com.arsylk.mammonsmite.Live2D.L2DModel;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DCSwapActivity extends ActivityWithExceptionRedirect {
    private interface OnModelItemPick {
        void onModelItemPick(L2DModelItem item);
    }
    private Context context = DCSwapActivity.this;
    private View fromModelView, toModelView;
    private L2DModelItem fromModelItem, toModelItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcswap);
        initViews();
    }

    private void initViews() {
        //pick model
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
                                fromModelItem = item;
                            }
                        });
                        break;
                    case R.id.item_to:
                        showPickModel(new OnModelItemPick() {
                            @Override
                            public void onModelItemPick(L2DModelItem item) {
                                fillModelView(item, toModelView);
                                toModelItem = item;
                            }
                        });
                        break;
                }
            }
        };
        fromModelView.setOnClickListener(onModelViewClick);
        toModelView.setOnClickListener(onModelViewClick);

        //swap click
        findViewById(R.id.swap_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toModelItem != null && fromModelItem != null) {
                    //TODO here actually starts
                    DCSwapper swapper = new DCSwapper(new L2DModel(fromModelItem.getFile()), new L2DModel(toModelItem.getFile()));
                    swapper.setOutputView((TextView) findViewById(R.id.scroll_output));
                    swapper.matchFiles();
                    Toast.makeText(context, swapper.swapModels() ? "Swap successful!" : "Swap failed!", Toast.LENGTH_SHORT).show();
                    //L2DModelsActivity.actionOpen(context, swapper.getLastSwapFolder());
                    //TODO here actually ends
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
