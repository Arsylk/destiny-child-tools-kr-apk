package com.arsylk.dcwallpaper.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.arsylk.dcwallpaper.R;

import java.util.List;

public class ResolveConflictsDialog extends AlertDialog.Builder {
    public static class Conflict {
        public interface OnConflictResolved {
            void onConflictResolved(Conflict conflict);
        }
        private String hash, key, valueOld, valueNew;
        private boolean resolve = true;

        public Conflict(String hash, String key, String valueOld, String valueNew) {
            this.hash = hash;
            this.key = key;
            this.valueOld = valueOld;
            this.valueNew = valueNew;
        }

        public String getHash() {
            return hash;
        }

        public String getKey() {
            return key;
        }

        public String getValueOld() {
            return valueOld;
        }

        public String getValueNew() {
            return valueNew;
        }

        public void setResolve(boolean resolve) {
            this.resolve = resolve;
        }

        public boolean getResolve() {
            return resolve;
        }

        public String resolve() {
            return resolve ? valueNew : valueOld;
        }

        @Override
        public String toString() {
            return String.format("'%s' => '%s'", valueOld, valueNew);
        }
    }

    private Context context;
    private List<Conflict> conflicts;
    private Conflict.OnConflictResolved onConflictResolved = null;
    private AlertDialog dialog = null;

    public ResolveConflictsDialog(Context context, List<Conflict> conflicts) {
        super(context);
        this.context = context;
        this.conflicts = conflicts;
        initViews();
    }

    public ResolveConflictsDialog setOnConflictResolved(Conflict.OnConflictResolved onConflictResolved) {
        this.onConflictResolved = onConflictResolved;
        return this;
    }

    private void initViews() {
        setTitle("Resolve conflicts");
        setCancelable(false);

        //list of conflicts
        ListView listView = new ListView(context);
        listView.setAdapter(new ArrayAdapter<Conflict>(context, R.layout.item_conflict, R.id.label, conflicts) {
            class ViewHolder {
                TextView labelarrow, labelleft, labelright, labeltop;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null) {
                    convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                            .inflate(R.layout.item_conflict, parent, false);
                    ViewHolder holder = new ViewHolder();
                    holder.labelarrow = convertView.findViewById(R.id.label_arrow);
                    holder.labelleft = convertView.findViewById(R.id.label_left);
                    holder.labelright = convertView.findViewById(R.id.label_right);
                    holder.labeltop = convertView.findViewById(R.id.label_top);

                    convertView.setTag(holder);
                }

                //get holder and conflict
                final ViewHolder holder = (ViewHolder) convertView.getTag();
                final Conflict conflict = getItem(position);

                //events
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean resolve = !conflict.getResolve();
                        holder.labelarrow.setText((resolve) ? ("=>") : ("<="));
                        holder.labelarrow.setTag(resolve);
                        conflict.setResolve(resolve);
                    }
                });
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(onConflictResolved != null) onConflictResolved.onConflictResolved(conflict);
                        conflicts.remove(conflict);
                        notifyDataSetChanged();

                        if(conflicts.size() == 0) {
                            if(dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        return true;
                    }
                });

                //update views
                holder.labelleft.setText(conflict.getValueOld());
                holder.labelright.setText(conflict.getValueNew());
                holder.labeltop.setText(conflict.getKey());

                return convertView;
            }
        });
        setView(listView);

        //buttons
        setPositiveButton("Resolve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(Conflict conflict : conflicts) {
                    if(onConflictResolved != null) onConflictResolved.onConflictResolved(conflict);
                }
            }
        });
        setNegativeButton("Cancel", null);

        dialog = create();
    }

    @Override
    public AlertDialog show() {
        dialog.show();
        return dialog;
    }
}
