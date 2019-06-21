package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.arsylk.dcwallpaper.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.arsylk.dcwallpaper.DestinyChild.DCTools.Resources.*;


public class SettingsActivity extends ActivityWithExceptionRedirect {
    private Context context = SettingsActivity.this;
    private LinearLayout layout;
    private PopupWindow disclaimer;
    private EditText inputStorage, inputPackage;
    private EditText inputFiles, inputModels, inputBackgrounds, inputSounds, inputLocale, inputModelInfo;
    private Button editFiles, editModels, editBackgrounds, editSounds, editLocale, editModelInfo;
    private List<Button> edits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        initDisclaimer();
    }

    private void initDisclaimer() {
        RelativeLayout disclaimerLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.dialog_disclaimer_popup, layout, false);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        disclaimer = new PopupWindow(disclaimerLayout, metrics.widthPixels, metrics.heightPixels, true);
        disclaimer.setBackgroundDrawable(new ColorDrawable(0xCC000000));
        disclaimer.getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disclaimer.dismiss();
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        disclaimer.showAtLocation(disclaimer.getContentView(), Gravity.CENTER, 0, 0);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disclaimer.dismiss();
    }

    private void initViews() {
        // init views
        layout = findViewById(R.id.settings_layout);
        inputStorage = findViewById(R.id.settings_input_storage);
        inputPackage = findViewById(R.id.settings_input_package);
        edits = new ArrayList<>();

        // input & edit
        inputFiles = findViewById(R.id.settings_input_files);
        editFiles = findViewById(R.id.settings_edit_files);
        editFiles.setTag(inputFiles);
        edits.add(editFiles);

        inputModels = findViewById(R.id.settings_input_models);
        editModels = findViewById(R.id.settings_edit_models);
        editModels.setTag(inputModels);
        edits.add(editModels);

        inputBackgrounds = findViewById(R.id.settings_input_backgrounds);
        editBackgrounds = findViewById(R.id.settings_edit_backgrounds);
        editBackgrounds.setTag(inputBackgrounds);
        edits.add(editBackgrounds);

        inputSounds = findViewById(R.id.settings_input_sounds);
        editSounds = findViewById(R.id.settings_edit_sounds);
        editSounds.setTag(inputSounds);
        edits.add(editSounds);

        inputLocale = findViewById(R.id.settings_input_locale);
        editLocale = findViewById(R.id.settings_edit_locale);
        editLocale.setTag(inputLocale);
        edits.add(editLocale);

        inputModelInfo = findViewById(R.id.settings_input_model_info);
        editModelInfo = findViewById(R.id.settings_edit_model_info);
        editModelInfo.setTag(inputModelInfo);
        edits.add(editModelInfo);


        // listeners
        View.OnClickListener onEditButtonClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view instanceof Button && view.getTag() instanceof EditText) {
                    Button edit = (Button) view;
                    EditText input = (EditText) view.getTag();

                    // reset state
                    clearState(view.getId());

                    // edit or save
                    if(input.isEnabled()) {
                        input.setEnabled(false);
                        input.setTextColor(updateAndCheckPath(input.getId(), input.getText().toString()) ? Color.GREEN : Color.RED);
                        edit.setText("Edit");
                    }else {
                        input.setEnabled(true);
                        input.setTextColor(Color.WHITE);
                        edit.setText("Save");
                    }
                }
            }
        };
        editFiles.setOnClickListener(onEditButtonClick);
        editModels.setOnClickListener(onEditButtonClick);
        editBackgrounds.setOnClickListener(onEditButtonClick);
        editSounds.setOnClickListener(onEditButtonClick);
        editLocale.setOnClickListener(onEditButtonClick);
        editModelInfo.setOnClickListener(onEditButtonClick);



        // storage resource
        inputStorage.setText(STORAGE_DIRECTORY);
        inputStorage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                inputStorage.setText(_STORAGE_DIRECTORY);
                update(_STORAGE_DIRECTORY, DESTINY_CHILD_PACKAGE);
                checkResourcePaths();
                return false;
            }
        });
        findViewById(R.id.settings_update_storage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(inputStorage.getText().toString(), DESTINY_CHILD_PACKAGE);
                checkResourcePaths();
            }
        });


        // package resource
        inputPackage.setText(DESTINY_CHILD_PACKAGE);
        inputPackage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                inputPackage.setText(_DESTINY_CHILD_PACKAGE);
                update(STORAGE_DIRECTORY, _DESTINY_CHILD_PACKAGE);
                checkResourcePaths();
                return false;
            }
        });
        findViewById(R.id.settings_update_package).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(STORAGE_DIRECTORY, inputPackage.getText().toString());
                checkResourcePaths();
            }
        });


        // fill views
        checkResourcePaths();
    }

    private void clearState(int ignoreId) {
        for(Button button : edits) {
            if(button.getId() == ignoreId) continue;
            ((EditText) button.getTag()).setTextColor(new File(((EditText) button.getTag()).getText().toString()).exists() ? Color.GREEN : Color.RED);
            ((EditText) button.getTag()).setEnabled(false);
            button.setText("Edit");
        }
    }

    private void checkResourcePaths() {
        clearState(0);
        inputFiles.setText(DC_FILES_DIRECTORY);
        inputFiles.setTextColor(new File(DC_FILES_DIRECTORY).exists() ? Color.GREEN : Color.RED);

        inputModels.setText(DC_MODELS_DIRECTORY);
        inputModels.setTextColor(new File(DC_MODELS_DIRECTORY).exists() ? Color.GREEN : Color.RED);

        inputBackgrounds.setText(DC_BACKGROUNDS_DIRECTORY);
        inputBackgrounds.setTextColor(new File(DC_BACKGROUNDS_DIRECTORY).exists() ? Color.GREEN : Color.RED);

        inputSounds.setText(DC_SOUNDS_DIRECTORY);
        inputSounds.setTextColor(new File(DC_SOUNDS_DIRECTORY).exists() ? Color.GREEN : Color.RED);

        inputLocale.setText(DC_LOCALE_FILE);
        inputLocale.setTextColor(new File(DC_LOCALE_FILE).exists() ? Color.GREEN : Color.RED);

        inputModelInfo.setText(DC_MODEL_INFO_FILE);
        inputModelInfo.setTextColor(new File(DC_MODEL_INFO_FILE).exists() ? Color.GREEN : Color.RED);
    }

    private boolean updateAndCheckPath(int resId, String text) {
        switch(resId) {
            case R.id.settings_input_files:
                DC_FILES_DIRECTORY = text;
                break;
            case R.id.settings_input_models:
                DC_MODELS_DIRECTORY = text;
                break;
            case R.id.settings_input_backgrounds:
                DC_BACKGROUNDS_DIRECTORY = text;
                break;
            case R.id.settings_input_sounds:
                DC_SOUNDS_DIRECTORY = text;
                break;
            case R.id.settings_input_locale:
                DC_LOCALE_FILE = text;
                break;
            case R.id.settings_input_model_info:
                DC_MODEL_INFO_FILE = text;
                break;
        }

        return new File(text).exists();
    }
}
