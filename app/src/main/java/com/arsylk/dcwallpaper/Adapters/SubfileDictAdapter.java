package com.arsylk.dcwallpaper.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.dcwallpaper.Async.interfaces.OnLocaleUnpackFinished;
import com.arsylk.dcwallpaper.Async.interfaces.OnPatchChangedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCLocalePatch;
import com.arsylk.dcwallpaper.DestinyChild.DCWiki;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubfileDictAdapter extends BaseAdapter implements Filterable {
    //list adapter
    private Context context;
    private DCLocalePatch.Subfile subfile;
    private List<Map.Entry<String, String>> entries;
    private DCLocalePatch patch = null;
    //search panel
    private TextView visibleLabel = null;
    private EditText keyField = null, valField = null, patchField = null;
    private ToggleButton toggleTranslated = null, toggleNonTranslated = null;
    private String keyQuery = "", valQuery = "", patchQuery = "";
    private boolean showTranslated = true, showNonTranslated = true;
    //patch callback
    private OnPatchChangedListener onPatchChangedListener = null;


    //constructors
    public SubfileDictAdapter(Context context) {
        this.context = context;
        this.entries = new ArrayList<>();
    }


    //setters
    public void setSearchPanel(View view) {
        final View layoutView = view.findViewById(R.id.search_layout);
        view.findViewById(R.id.search_toggle_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVisible = layoutView.getVisibility() == View.VISIBLE;
                v.setScaleX(isVisible ? -1.0f : 1.0f);
                v.setScaleY(isVisible ? -1.0f : 1.0f);
                layoutView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                if(isVisible)
                    Utils.dismissKeyboard(context);
            }
        });

        visibleLabel = view.findViewById(R.id.search_visible_label);

        keyField = view.findViewById(R.id.search_key_field);
        keyField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyQuery = s.toString();
                getFilter().filter(null);
            }
        });

        valField = view.findViewById(R.id.search_val_field);
        valField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valQuery = s.toString();
                getFilter().filter(null);
            }
        });
        valField.setText("");

        patchField = view.findViewById(R.id.search_patch_field);
        patchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                patchQuery = s.toString();
                getFilter().filter(null);
            }
        });

        toggleTranslated = view.findViewById(R.id.toggle_translated);
        toggleTranslated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showTranslated = isChecked;
                getFilter().filter(null);
            }
        });

        toggleNonTranslated = view.findViewById(R.id.toggle_non_translated);
        toggleNonTranslated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showNonTranslated = isChecked;
                getFilter().filter(null);
            }
        });
    }

    public void setSubfile(DCLocalePatch.Subfile subfile) {
        this.subfile = subfile;
        if(subfile != null) {
            this.entries = new ArrayList<>(subfile.getDict().entrySet());
        }else {
            this.entries = new ArrayList<>();
        }
        resetSearchPanel();
        notifyDataSetChanged();
    }

    public void setOnPatchChangedListener(OnPatchChangedListener onPatchChangedListener) {
        this.onPatchChangedListener = onPatchChangedListener;
    }


    //methods
    public void applyPatch(DCLocalePatch patch) {
        this.patch = patch;
        notifyDataSetChanged();
    }

    private void updatePatch(String key, String val) {
        if(patch == null) {
            //if no patch create
            patch = new DCLocalePatch();
        }
        if(patch.getHashFile(subfile.getHash()) == null) {
            //if no subfile add
            patch.addSubfile(new DCLocalePatch.Subfile(subfile.getHash(), subfile.getLineType(), new LinkedHashMap<String, String>()));
        }
        if(patch.getHashFileDictValue(subfile.getHash(), key) != null) {
            //if same as in patch
            if(patch.getHashFileDictValue(subfile.getHash(), key).equals(val)) {
                return;
            }
        }
        //update entry in patch
        if(val != null) {
            patch.getHashFile(subfile.getHash()).setValue(key, val);
        }else {
            if(patch.getHashFile(subfile.getHash()) != null) {
                patch.getHashFile(subfile.getHash()).delValue(key);
                if(patch.getHashFileDict(subfile.getHash()).size() == 0) {
                    patch.delSubfile(patch.getHashFile(subfile.getHash()));
                }
            }
        }

        //notify about patch update
        if(onPatchChangedListener != null) onPatchChangedListener.onPatchChanged(patch, subfile, key, val);
    }

    private void resetSearchPanel() {
        if(keyField != null)
            keyField.setText("");
        if(valField != null)
            valField.setText("");
        if(patchField != null)
            patchField.setText("");
        if(toggleTranslated != null)
            toggleTranslated.setChecked(true);
        if(toggleNonTranslated != null)
            toggleNonTranslated.setChecked(true);
    }

    //overrides
    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_keyvaluebox, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.keylabel = convertView.findViewById(R.id.key_label);
            holder.defaultfield = convertView.findViewById(R.id.default_field);
            holder.translatedfield = convertView.findViewById(R.id.translated_field);
            holder.watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
                @Override
                public void afterTextChanged(Editable s) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePatch(holder.keylabel.getText().toString(), s.toString());
                }
            };
            convertView.setTag(holder);
        }

        //get holder and entry
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Map.Entry<String, String> entry = getItem(position);

        //update views
        holder.keylabel.setText(entry.getKey());
        holder.translatedfield.removeTextChangedListener(holder.watcher);
        holder.defaultfield.setText(entry.getValue());
        if(patch != null) {
            String formatted = patch.getHashFileDictValue(subfile.getHash(), entry.getKey());
            holder.translatedfield.setText(formatted);
        }else {
            holder.translatedfield.setText("");
        }
        holder.translatedfield.addTextChangedListener(holder.watcher);
        holder.translatedfield.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, "Removed entry from patch!", Toast.LENGTH_SHORT).show();
                if(v instanceof EditText) {
                    ((EditText) v).setText("");
                }
                updatePatch(holder.keylabel.getText().toString(), null);

                return true;
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView keylabel, defaultfield;
        EditText translatedfield;
        TextWatcher watcher;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                FilterResults results = new FilterResults();
                List<Map.Entry<String, String>> filterList = new ArrayList<>();
                for(Map.Entry<String, String> entry : subfile.getDict().entrySet()) {
                    //check key query
                    if(!entry.getKey().toLowerCase().contains(keyQuery.toLowerCase()))
                        continue;
                    //check val query
                    if(!entry.getValue().toLowerCase().contains(valQuery.toLowerCase()))
                        continue;

                    //load patch
                    if(patch != null) {
                        String patchValue = patch.getHashFileDictValue(subfile.getHash(), entry.getKey());
                        if(patchValue != null) {
                            //check toggle patch
                            if(!showTranslated)
                                continue;
                            //check patch query
                            if(!patchValue.toLowerCase().contains(patchQuery.toLowerCase()))
                                continue;
                        }else {
                            if(!showNonTranslated)
                                continue;
                        }
                    }

                    //add if checked
                    filterList.add(entry);
                }
                results.count = filterList.size();
                results.values = filterList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if(results != null) {
                    if(results.values != null) {
                        entries = (List<Map.Entry<String, String>>) results.values;
                        notifyDataSetChanged();
                    }
                }
            }
        };
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(visibleLabel != null && subfile != null) {
            int entryCount = 0, translatedCount = 0;
            for(Map.Entry<String, String> entry : entries) {
                entryCount+=1;
                if(patch != null) {
                    if(patch.getHashFileDictValue(subfile.getHash(), entry.getKey()) != null) {
                        translatedCount+=1;
                    }
                }
            }
            int allEntries = 0, allTranslated = 0;
            if(subfile.getDict() != null) {
                allEntries = subfile.getDict().size();
            }
            if(patch != null) {
                if(patch.getHashFileDict(subfile.getHash()) != null) {
                    allTranslated = patch.getHashFileDict(subfile.getHash()).size();
                }
            }

            visibleLabel.setText(String.format("Displaying %d/%d ouf of %d/%d", translatedCount, entryCount, allTranslated, allEntries));
        }
    }
}
