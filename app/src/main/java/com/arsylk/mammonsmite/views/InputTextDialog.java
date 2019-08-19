package com.arsylk.mammonsmite.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class InputTextDialog extends AlertDialog.Builder {
    public interface OnInputSubmitted {
        void onInputSubmitted(String text);
    }

    private Context context;
    private AlertDialog dialog = null;
    private EditText input = null;
    private OnInputSubmitted onInputSubmitted = null;

    public InputTextDialog(Context context, String title, String hint, OnInputSubmitted onInputSubmitted) {
        super(context);
        this.context = context;
        this.onInputSubmitted = onInputSubmitted;
        initViews(title, hint);
    }

    private void initViews(String title, String hint) {
        setTitle(title);
        input = new EditText(context);
        input.setLines(1);
        input.setHint(hint);
        setView(input);
        setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onInputSubmitted != null) {
                    onInputSubmitted.onInputSubmitted(input.getText().toString());
                }
                dialog.dismiss();
            }
        });
        setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = create();
    }

    @Override
    public AlertDialog show() {
        dialog.show();
        return dialog;
    }
}
