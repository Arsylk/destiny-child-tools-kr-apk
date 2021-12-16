package com.arsylk.mammonsmite.views;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.*;
import android.widget.*;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Define;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

public class BigTextDialog extends AlertDialog.Builder {
    private Context context;
    private String title;
    private String bigText;
    private String[] lines;
    private String filename = null;


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

        //meme recycler view solution
        setView(initBigTextView(context, lines));


        //buttons
        setPositiveButton("Save", (dialog, which) -> {
            try {
                String name = filename != null ? filename : title+"_"+System.currentTimeMillis()+".json";
                File file = new File(Define.BASE_DIRECTORY, name);
                FileUtils.write(file, bigText, Charset.forName("utf-8"));
                Toast.makeText(context, "Saved to: "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }catch(Exception e) {
                e.printStackTrace();
            }
        });
        setNeutralButton("Copy", (dialog, which) -> {
            try {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(filename != null ? filename : title, bigText);
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
            }catch(Exception e) {
                e.printStackTrace();
            }
        });
        setNegativeButton("Cancel", null);
    }

    public static RecyclerView initBigTextView(final Context context, String bigText) {
        return initBigTextView(context, bigText.split("\n"));
    }

    public static RecyclerView initBigTextView(final Context context, String[] lines) {
        class LineAdapter extends RecyclerView.Adapter<LineAdapter.Holder> {
            class Holder extends RecyclerView.ViewHolder {
                protected TextView label;
                private Holder(View view) {
                    super(view);
                    this.label = view.findViewById(R.id.label);
                }
            }
            protected String[] rows;
            public LineAdapter(String[] lines) {
                this.rows = lines;
            }
            @Override
            public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
                return new LineAdapter.Holder(LayoutInflater.from(context).inflate(R.layout.item_textline, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(final Holder holder, int i) {
                holder.label.setText(rows[i]);
            }

            @Override
            public int getItemCount() {
                return rows.length;
            }
        }
        class LineListAdapter extends RecyclerView.Adapter<LineListAdapter.Holder> {
            class Holder extends RecyclerView.ViewHolder {
                protected RecyclerView rv;
                private Holder(View view) {
                    super(view);
                    this.rv = (RecyclerView) view;
                    int pad = context.getResources().getDimensionPixelSize(R.dimen.activity_margin);
                    rv.setPadding(pad, pad, pad, pad);
                }
            }
            protected String[] lines;
            public LineListAdapter(String[] lines) {
                this.lines = lines;
            }

            @Override
            public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
                return new LineListAdapter.Holder(new RecyclerView(context));
            }

            @Override
            public void onBindViewHolder(final Holder holder, int i) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false);
                holder.rv.setLayoutManager(layoutManager);
                holder.rv.setAdapter(new LineAdapter(lines));
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        }

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(new LineListAdapter(lines));

        return recyclerView;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
