package com.arsylk.dcwallpaper.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.arsylk.dcwallpaper.Adapters.DCModelItem;
import com.arsylk.dcwallpaper.Adapters.DCModelsAdapter;
import com.arsylk.dcwallpaper.Async.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import com.arsylk.dcwallpaper.views.SaveModelDialog;

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
        search_advance_layout = findViewById(R.id.search_advance_layout);

        search_advance_elements = findViewById(R.id.search_advance_elements_layout);
        for(int i = 0; i < search_advance_elements.getChildCount(); i++) {
            search_advance_elements.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(adapter != null) {
                        view.setAlpha(view.getAlpha() == 1.0f ? 0.3f : 1.0f);
                        adapter.applyFilterOption(view.getId());
                    }
                }
            });
        }
        search_advance_types = findViewById(R.id.search_advance_types_layout);
        for(int i = 0; i < search_advance_types.getChildCount(); i++) {
            search_advance_types.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(adapter != null) {
                        view.setAlpha(view.getAlpha() == 1.0f ? 0.3f : 1.0f);
                        adapter.applyFilterOption(view.getId());
                    }
                }
            });
        }


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
        search_input.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(adapter != null) {
                    Utils.dismissKeyboard(context);
                    search_advance_layout.setVisibility((search_advance_layout.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
                    adapter.applyFilterOption(R.id.search_advance_layout);
                    return true;
                }
                return false;
            }
        });

        //TODO find a better way for dealing with that
        model_list = findViewById(R.id.model_list);
        model_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(LoadAssets.getDCWikiInstance().hasWikiPage(adapter.getItem(i).getModelId())) {
                    DCModelsActivity.showModelOrWiki(context, adapter.getItem(i));
                }else {
                    DCTools.asyncUnpack(adapter.getItem(i).getFile(), context, new OnUnpackFinishedListener() {
                        @Override
                        public void onFinished(DCModel dcModel) {
                            if(dcModel != null) {
                                if(dcModel.isLoaded()) {
                                    DCModelsActivity.showPickAction(context, dcModel);
                                    return;
                                }
                            }
                            Toast.makeText(context, "Failed to unpack!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

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

    public static void showModelOrWiki(final Context context, final DCModelItem dcModelItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose action:");
        builder.setItems(new String[]{"Wiki", "Unpack"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0:
                        context.startActivity(new Intent(context, DCWikiPageActivity.class).putExtra("model_id", dcModelItem.getModelId()));
                        break;
                    case 1:
                        DCTools.asyncUnpack(dcModelItem.getFile(), context, new OnUnpackFinishedListener() {
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
            }
        });
        builder.create().show();
    }

    public static void showPickAction(final Context context, final DCModel dcModel) {
        final DCModelItem dcModelItem = new DCModelItem(dcModel.getOutput());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose action:");
        builder.setItems(new String[]{"Open", "Preview", "Save"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i) {
                    case 0:
                        //open folder
                        try {
                            StrictMode.class.getMethod("disableDeathOnFileUriExposure").invoke(null);
                        }catch(Exception e) {
                        }
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW)
                                    .setFlags(Intent.URI_ALLOW_UNSAFE)
                                    .setDataAndType(Uri.fromFile(dcModel.getOutput()), "resource/folder"));
                        }catch(Exception e) {
                            Toast.makeText(context, "No file explorer found!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        //open peek mode preview
                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString("model_id", dcModelItem.isLoaded() ? (dcModelItem.getModelId()+"_"+dcModelItem.getModelFlag()) : dcModel.getOutput().getName())
                                .putString("preview_model", dcModel.getModelFile().getFile().getAbsolutePath())
                                .commit();
                        context.startActivity(new Intent(context, L2DPreviewActivity.class).putExtra("mode", L2DConfig.MODE_PEEK));
                        break;
                    case 2:
                        //open save model dialog
                        new SaveModelDialog(context, new L2DModel(dcModel.getModelFile().getFile())).showDialog();
                        break;
                }
            }
        });
        builder.create().show();
    }
}
