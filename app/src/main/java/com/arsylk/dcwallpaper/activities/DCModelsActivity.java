package com.arsylk.dcwallpaper.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.arsylk.dcwallpaper.Adapters.DCModelItem;
import com.arsylk.dcwallpaper.Adapters.DCModelsAdapter;
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.views.PickWhichDialog;
import com.arsylk.dcwallpaper.views.SaveModelDialog;

import java.util.ArrayList;
import java.util.List;

public class DCModelsActivity extends AppCompatActivity {
    private Context context = DCModelsActivity.this;
    private EditText search_input;
    private ViewGroup search_advance_layout, search_advance_elements, search_advance_types;

    private ListView model_list;
    private volatile DCModelsAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_dcmodels);
        initViews();
    }

    private void initViews() {
        search_input = findViewById(R.id.search_input);
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        model_list = findViewById(R.id.model_list);
        model_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DCTools.asyncUnpack(adapter.getItem(i).getFile(), context, new OnUnpackFinishedListener() {
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
            }
        });

        //populate adapter in background
        new Thread(new Runnable() {
            @Override
            public void run() {
                adapter = new DCModelsAdapter(context, DCTools.getDCModelsPath());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        model_list.setAdapter(adapter);
                    }
                });
            }
        }).start();

    }

    public static void showPickAction(final Context context, final L2DModel l2DModel) {
        //create options
        List<PickWhichDialog.Option<Integer>> options = new ArrayList<>();
        options.add(new PickWhichDialog.Option<Integer>("Open", 0));
        options.add(new PickWhichDialog.Option<Integer>("Preview", 1));
        options.add(new PickWhichDialog.Option<Integer>("Save", 2));

        //create dialog
        PickWhichDialog<Integer> pickAction = new PickWhichDialog<>(context, options);
        pickAction.setTitle("Pick Action");
        pickAction.setOnOptionPicked(new PickWhichDialog.Option.OnOptionPicked<Integer>() {
            @Override
            public void onOptionPicked(PickWhichDialog.Option<Integer> option) {
                switch(option.getObject()) {
                    //open unpack folder
                    case 0:
                        L2DModelsActivity.actionOpen(context, l2DModel);
                        break;
                    //peek without saving
                    case 1:
                        actionPeek(context, l2DModel);
                        break;
                    //save model
                    case 2:
                        new SaveModelDialog(context, l2DModel).showDialog();
                        break;
                }
            }
        });
        pickAction.show();
    }

    private static void actionPeek(Context context, L2DModel l2DModel) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("model_id", l2DModel.getModelId())
                .putString("preview_model", l2DModel.getModel().getAbsolutePath())
                .commit();
        context.startActivity(new Intent(context, L2DPreviewActivity.class).putExtra("mode", L2DConfig.MODE_PEEK));
    }
}
