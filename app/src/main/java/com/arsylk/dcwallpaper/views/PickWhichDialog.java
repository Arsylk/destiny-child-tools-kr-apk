package com.arsylk.dcwallpaper.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class PickWhichDialog<T> extends AlertDialog.Builder {
    public static class Option<T> {
        public interface OnOptionPicked<T> {
            void onOptionPicked(Option<T> option);
        }
        private String label;
        private T object;

        public Option(String label, T object) {
            this.label = label;
            this.object = object;
        }

        public String getLabel() {
            return label;
        }

        public T getObject() {
            return object;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Context context;
    private List<Option<T>> options;
    private Option.OnOptionPicked<T> onOptionPicked = null;
    private AlertDialog dialog = null;

    public PickWhichDialog(Context context, List<Option<T>> options) {
        super(context);
        this.context = context;
        this.options = options;
        initViews();
    }

    public PickWhichDialog<T> setOnOptionPicked(Option.OnOptionPicked<T> onOptionPicked) {
        this.onOptionPicked = onOptionPicked;
        return this;
    }

    private void initViews() {
        setTitle("Pick option");
        ListView listView = new ListView(context);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, options));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(dialog != null) dialog.dismiss();
                if(onOptionPicked != null) onOptionPicked.onOptionPicked(options.get(position));
            }
        });
        setView(listView);
        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onOptionPicked != null) onOptionPicked.onOptionPicked(null);
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
