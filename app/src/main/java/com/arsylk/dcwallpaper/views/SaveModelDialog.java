package com.arsylk.dcwallpaper.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.DCModelItem;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCModelInfo;
import com.arsylk.dcwallpaper.Live2D.L2DModel;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.LoadAssets;
import com.arsylk.dcwallpaper.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class SaveModelDialog extends AlertDialog.Builder implements Dialog.OnShowListener {
    private AlertDialog dialog;
    private L2DModel l2DModel;
    private EditText input_name, input_id, input_folder;
    private Utils.Callback callback;

    public SaveModelDialog(Context context, L2DModel l2DModel) {
        super(context);
        this.l2DModel = l2DModel;
        DCModelItem dcModelItem = new DCModelItem(l2DModel.getOutput());
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_save_model, null);
        input_name = view.findViewById(R.id.input_name);
        input_id = view.findViewById(R.id.input_id);
        input_folder = view.findViewById(R.id.input_folder);

        input_name.setText(dcModelItem.getFormatted());
        input_id.setText(dcModelItem.isLoaded() ? (dcModelItem.getModelId()+"_"+dcModelItem.getModelFlag()) : l2DModel.getOutput().getName());
        input_folder.setText(dcModelItem.getFormatted().replace(" ", "_").toLowerCase());

        setTitle("Save model");
        setView(view);
        setCancelable(true);
        setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        setPositiveButton("Save", null);
    }

    public void setOnModelSavedListener(Utils.Callback callback) {
        this.callback = callback;
    }

    public void showDialog() {
        dialog = create();
        dialog.setOnShowListener(this);
        dialog.show();
        dialog.setOnShowListener(this);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File unpackPath = new File(Define.MODELS_DIRECTORY, input_folder.getText().toString());
                if(!unpackPath.exists()) {
                    try {
                        FileUtils.moveDirectory(l2DModel.getOutput(), unpackPath);
                        DCModel.generateModel(unpackPath.getAbsolutePath(), input_id.getText().toString(), input_name.getText().toString());
                        Toast.makeText(getContext(), "Saved to: "+unpackPath.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        if(callback != null) callback.onCall();
                        dialog.dismiss();
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(getContext(), "Folder already exists!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
