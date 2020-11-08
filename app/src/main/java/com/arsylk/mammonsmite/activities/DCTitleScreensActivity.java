package com.arsylk.mammonsmite.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.mammonsmite.Adapters.DCTitleScreensAdapter;
import com.arsylk.mammonsmite.DestinyChild.DCTitleScreen;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.R;

public class DCTitleScreensActivity extends ActivityWithExceptionRedirect {
    private Context context = DCTitleScreensActivity.this;
    private EditText searchInput;
    private ListView listView;
    private volatile DCTitleScreensAdapter adapter = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_dctitle_screen);
        initViews();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
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

        listView = findViewById(R.id.dctitle_screen_list);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                DCTitleScreen titleScreen = new DCTitleScreen(adapter.getItem(i).getFile());
                if(titleScreen.isValid()) {
                    editTitleScreen(titleScreen);
                }else {
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //populate adapter in background
        new Thread(() -> {
            adapter = new DCTitleScreensAdapter(context, DCTools.getDCTitleScreensPath());
            runOnUiThread(() -> listView.setAdapter(adapter));
        }).start();
    }

    private void editTitleScreen(DCTitleScreen titleScreen) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_title_screen_edit, null, false);
        EditText editViewIdx = view.findViewById(R.id.input_view_idx);
        editViewIdx.setText(titleScreen.getViewIdx());
        EditText editStage = view.findViewById(R.id.input_stage);
        editStage.setText(titleScreen.getStage());
        EditText editX = view.findViewById(R.id.input_x);
        editX.setText(String.valueOf(titleScreen.getX()));
        EditText editY = view.findViewById(R.id.input_y);
        editY.setText(String.valueOf(titleScreen.getY()));
        EditText editXScale = view.findViewById(R.id.input_x_scale);
        editXScale.setText(String.valueOf(titleScreen.getXScale()));
        EditText editYScale = view.findViewById(R.id.input_y_scale);
        editYScale.setText(String.valueOf(titleScreen.getYScale()));



        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleScreen.getFile().getName());
        builder.setView(view);
        builder.setPositiveButton("Save", (dialogInterface, which) -> {
            dialogInterface.dismiss();
            Toast.makeText(context, "Saved !", Toast.LENGTH_SHORT).show();
        });
        builder.show();

    }
}
