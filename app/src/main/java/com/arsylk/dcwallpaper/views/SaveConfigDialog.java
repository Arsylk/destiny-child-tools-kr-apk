package com.arsylk.dcwallpaper.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.Live2D.L2DConfig;
import com.arsylk.dcwallpaper.R;

import java.io.File;

public class SaveConfigDialog extends AlertDialog.Builder implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {
    private Context context;
    private L2DConfig config;
    private EditText input_bg_path;
    private EditText input_scale, input_offset_x, input_offset_y;
    private CheckBox check_animated, check_tappable, check_sounds;
    private int[] radio_bg_mode_ids = new int[] {R.id.radio_bg_0, R.id.radio_bg_1, R.id.radio_bg_2};
    private RadioButton[] radio_bg_modes = new RadioButton[3];

    private Dialog dialog = null;

    public SaveConfigDialog(Context context, L2DConfig config) {
        super(context);
        this.context = context;
        this.config = config;
        initViews();
    }

    private void initViews() {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_save_config, null);

        input_bg_path = view.findViewById(R.id.input_bg_path);
        input_bg_path.setText(config.getBackgroundPath());
        input_bg_path.setOnFocusChangeListener(this);
        input_bg_path.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                File file = DCTools.getRandomDCBackground();
                if(file != null) {
                    input_bg_path.setText(file.getAbsolutePath());
                    input_bg_path.onWindowFocusChanged(false);
                }
                return false;
            }
        });

        input_scale = view.findViewById(R.id.input_scale);
        input_scale.setText(String.valueOf(config.getModelScale()));
        input_scale.setOnFocusChangeListener(this);

        input_offset_x = view.findViewById(R.id.input_offset_x);
        input_offset_x.setText(String.valueOf(config.getModelOffsetX()));
        input_offset_x.setOnFocusChangeListener(this);

        input_offset_y = view.findViewById(R.id.input_offset_y);
        input_offset_y.setText(String.valueOf(config.getModelOffsetY()));
        input_offset_y.setOnFocusChangeListener(this);

        check_animated = view.findViewById(R.id.check_animated);
        check_animated.setChecked(config.isAnimated());
        check_animated.setOnCheckedChangeListener(this);

        check_tappable = view.findViewById(R.id.check_tappable);
        check_tappable.setChecked(config.isTappable());
        check_tappable.setOnCheckedChangeListener(this);

        check_sounds = view.findViewById(R.id.check_sounds);
        check_sounds.setChecked(config.isSounds());
        check_sounds.setOnCheckedChangeListener(this);

        for(int i = 0; i < radio_bg_modes.length; i++) {
            radio_bg_modes[i] = view.findViewById(radio_bg_mode_ids[i]);
            radio_bg_modes[i].setOnCheckedChangeListener(this);
        }
        radio_bg_modes[config.getBgMode()].setChecked(true);


        setTitle("Configurate");
        setView(view);
        setCancelable(true);
        setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                config.writeToModel();
                Toast.makeText(context, "Config saved!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog = create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onFocusChange(View view, boolean focused) {
        if(focused)
            return;
        if(view instanceof EditText) {
            EditText editText = (EditText) view;
            if((editText.getInputType() & InputType.TYPE_CLASS_NUMBER) != 0) {
                //numerical input
                float value = Float.NaN;
                try {
                    value = Float.parseFloat(editText.getText().toString());
                }catch(Exception e) {
                    editText.getText().clear();
                }
                if(editText.getText().toString().isEmpty()) {
                    switch(view.getId()) {
                        case R.id.input_scale:
                            input_scale.setText(String.valueOf(config.getModelScale()));
                            break;
                        case R.id.input_offset_x:
                            input_offset_x.setText(String.valueOf(config.getModelOffsetX()));
                            break;
                        case R.id.input_offset_y:
                            input_offset_y.setText(String.valueOf(config.getModelOffsetY()));
                            break;
                    }
                }else if(!Float.isNaN(value)) {
                    switch(view.getId()) {
                        case R.id.input_scale:
                            config.setModelScale(value);
                            break;
                        case R.id.input_offset_x:
                            config.setModelOffsetX(value);
                            break;
                        case R.id.input_offset_y:
                            config.setModelOffsetY(value);
                            break;
                    }
                }
            }else if((editText.getInputType() & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
                //file path input
                File value = new File(editText.getText().toString());
                if(!value.exists()) {
                    switch(view.getId()) {
                        case R.id.input_bg_path:
                            input_bg_path.setText(config.getBackgroundPath());
                            break;
                    }
                }else {
                    switch(view.getId()) {
                        case R.id.input_bg_path:
                            if(!config.getBackgroundPath().equals(value.getAbsolutePath())) {
                                config.setBackgroundPath(value.getAbsolutePath());
                                config.requestReload();
                            }
                            break;
                    }
                }
            }

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if(compoundButton instanceof CheckBox) {
            switch(compoundButton.getId()) {
                case R.id.check_animated:
                    config.setAnimated(checked);
                    break;
                case R.id.check_tappable:
                    config.setTappable(checked);
                    break;
                case R.id.check_sounds:
                    config.setSounds(checked);
                    break;
            }
        }else if(compoundButton instanceof RadioButton) {
            if(!checked || !compoundButton.isShown()) return;
            for(int i = 0; i < radio_bg_modes.length; i++) {
                boolean radioChecked = radio_bg_modes[i].getId() == compoundButton.getId();
                radio_bg_modes[i].setChecked(radioChecked);
                if(radioChecked) {
                    config.setBgMode(i);
                    config.requestReload();
                }
            }
        }
    }

    public void showDialog() {
        dialog.show();
    }
}
