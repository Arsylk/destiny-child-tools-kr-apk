package com.arsylk.dcwallpaper.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

public class BigTextDialog extends AlertDialog.Builder {
    private Context context;
    private String title;
    private String bigText;
    private String[] lines;

    public BigTextDialog(Context context, String title, String bigText) {
        super(context);
        this.context = context;
        this.title = title;
        this.bigText = bigText;
        this.lines = bigText.split("\n");
        initViews();
    }

    private void initViews() {
        //dialog parameters
        setTitle(title);
        setCancelable(false);

        //list view with lines
        ListView listView = new ListView(context);
        int padding = (int) context.getResources().getDimension(R.dimen.item_margin);
        listView.setPadding(0, padding, 0, padding);
        listView.setDividerHeight(0);
        listView.setClickable(false);
        listView.setSelector(android.R.color.transparent);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setAdapter(new ArrayAdapter<>(context, R.layout.item_textline, R.id.label, lines));
        setView(listView);

        //buttons
        setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    File file = new File(Define.BASE_DIRECTORY, title+"_"+System.currentTimeMillis()+".json");
                    FileUtils.write(file, bigText, Charset.forName("utf-8"));
                    Toast.makeText(context, "Saved to: "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        setNegativeButton("Cancel", null);
    }
}
