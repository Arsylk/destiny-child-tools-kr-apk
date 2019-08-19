package com.arsylk.mammonsmite.Adapters;

import android.content.Context;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.mammonsmite.Async.interfaces.OnPatchChangedListener;
import com.arsylk.mammonsmite.DestinyChild.DCLocalePatch;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.utils.Utils;
import com.arsylk.mammonsmite.views.PopupEditText;
import com.arsylk.mammonsmite.views.TabSpannableFactory;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubfileDictAdapter extends BaseAdapter implements Filterable {
    //list adapter
    private Context context;
    private DCLocalePatch.Subfile subfile = null;
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
        this.patch = new DCLocalePatch();
    }

    //setters
    public void setSearchPanel(View view) {
        final View layoutView = view.findViewById(R.id.search_layout);
        View showHideView = view.findViewById(R.id.search_toggle_layout);
        showHideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isVisible = layoutView.getVisibility() == View.VISIBLE;
                v.setScaleX(isVisible ? -1.0f : 1.0f);
                v.setScaleY(isVisible ? -1.0f : 1.0f);
                layoutView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                Utils.dismissKeyboard(context);
            }
        });
        showHideView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetSearchPanel();
                return true;
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
        getFilter().filter(null);
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

    public void updatePatch(String key, String val) {
        //notify about patch update
        if(onPatchChangedListener != null) {
            onPatchChangedListener.onPatchChanged(patch, subfile, key, val);
        }

        //update inner patch
        if(val != null) {
            if(patch.getHashFile(subfile.getHash()) == null) {
                patch.addSubfile(new DCLocalePatch.Subfile(subfile.getHash(), subfile.getLineType(), new LinkedHashMap<String, String>()));
            }
            patch.getHashFile(subfile.getHash()).setValue(key, val);
        }else {
            if(patch.getHashFileDictValue(subfile.getHash(), key) != null) {
                patch.getHashFile(subfile.getHash()).delValue(key);
                if(patch.getHashFileDict(subfile.getHash()).size() == 0) {
                    patch.delSubfile(patch.getHashFile(subfile.getHash()));
                }
            }
        }
    }

    public void resetSearchPanel() {
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
        getFilter().filter(null);
    }

    //overrides
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(visibleLabel != null && subfile != null) {
            visibleLabel.setText(String.format("Displaying %d out of %d", entries.size(), subfile.getDict().size()));
        }
    }

    //getters
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
        //inflate view
        if(convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_keyvaluebox, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.keylabel = convertView.findViewById(R.id.key_label);
            holder.defaultfield = convertView.findViewById(R.id.default_field);
            holder.popupfield = convertView.findViewById(R.id.popup_field);

            convertView.setTag(holder);
        }

        //get holder and entry
        final ViewHolder holder = (ViewHolder) convertView.getTag();
        final Map.Entry<String, String> entry = getItem(position);

        //update key label
        holder.keylabel.setText(entry.getKey());
        holder.keylabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Utils.translate(context, entry.getValue(), new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if(e == null) {
                            holder.popupfield.setText(result, TextView.BufferType.SPANNABLE);
                        }else {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return false;
            }
        });

        //update value field
        holder.defaultfield.setSpannableFactory(new TabSpannableFactory());
        holder.defaultfield.setText(entry.getValue(), TextView.BufferType.SPANNABLE);

        //update popup translated field
        holder.popupfield.setDialogTitle(entry.getKey());
        holder.popupfield.setSpannableFactory(new TabSpannableFactory());
        holder.popupfield.setText(patch.getHashFileDictValue(subfile.getHash(), entry.getKey()), TextView.BufferType.SPANNABLE);
        holder.popupfield.setOnTextChangedListener(new PopupEditText.OnTextChangedListener() {
            @Override
            public void onTextChanged(String text) {
                updatePatch(entry.getKey(), !text.isEmpty() ? text : null);
            }
        });
        holder.popupfield.setOnTitleLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                holder.popupfield.setDialogText(entry.getValue());
                return true;
            }
        });


        return convertView;
    }

    static class ViewHolder {
        TextView keylabel, defaultfield;
        PopupEditText popupfield;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                if(subfile == null) return null;
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

    public DCLocalePatch.Subfile getSubfile() {
        return subfile;
    }
}
