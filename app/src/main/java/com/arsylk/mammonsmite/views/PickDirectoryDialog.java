package com.arsylk.mammonsmite.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.mammonsmite.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class PickDirectoryDialog extends AlertDialog.Builder {
    private Context context;
    private AlertDialog dialog;
    private DirectoriesAdapter adapter;
    private Utils.OnPostExecute<File> callback = null;

    public PickDirectoryDialog(Context context, File directory) {
        super(context);
        this.context = context;
        this.adapter = new DirectoriesAdapter(context);
        this.adapter.setDirectory(directory == null ? Environment.getExternalStorageDirectory() : directory);

        initViews();
    }


    private void initViews() {
        ListView listView = new ListView(context);
        listView.setAdapter(adapter);

        View headerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(android.R.layout.simple_list_item_1, null, false);
        TextView textViewHeader = headerView.findViewById(android.R.id.text1);
        textViewHeader.setText("..");
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter != null) {
                    File currentDirectory = adapter.getDirectory();
                    if(currentDirectory.getParentFile() != null) {
                        if(currentDirectory.getParentFile().listFiles() != null) {
                            adapter.setDirectory(currentDirectory.getParentFile());
                        }
                    }
                }
            }
        });

        listView.addHeaderView(headerView);
        setView(listView);
        setCancelable(false);

        setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(callback != null) {
                    callback.onPostExecute(adapter.getDirectory());
                }
            }
        });
        setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = create();
        adapter.setDialog(dialog);
    }

    public PickDirectoryDialog setCallback(Utils.OnPostExecute<File> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public AlertDialog show() {
        dialog.show();
        return dialog;
    }

    class DirectoriesAdapter extends BaseAdapter {
        private Context context;
        private AlertDialog dialog;
        private File directory;
        private List<File> directoryContent;

        public DirectoriesAdapter(Context context) {
            this.context = context;
            setDirectory(Environment.getExternalStorageDirectory());
        }

        public void setDialog(AlertDialog dialog) {
            this.dialog = dialog;
            if(dialog != null) {
                dialog.setTitle(directory.getAbsolutePath());
            }
        }

        public void setDirectory(File directory) {
            this.directory = directory;
            directoryContent = new ArrayList<>();


            List<File> contentFolders = Arrays.asList(this.directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            }));
            Collections.sort(contentFolders, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
            List<File> contentFiles = Arrays.asList(this.directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            }));
            Collections.sort(contentFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });

            directoryContent.addAll(contentFolders);
            directoryContent.addAll(contentFiles);

            if(dialog != null) {
                dialog.setTitle(directory.getAbsolutePath());
            }
            notifyDataSetChanged();
        }

        public File getDirectory() {
            return directory;
        }

        @Override
        public int getCount() {
            return directoryContent.size();
        }

        @Override
        public File getItem(int position) {
            return directoryContent.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView textView = convertView.findViewById(android.R.id.text1);

            textView.setTag(getItem(position));
            textView.setText(getItem(position).getName());
            textView.setEnabled(getItem(position).isDirectory());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DirectoriesAdapter.this.setDirectory((File) v.getTag());
                }
            });

            return convertView;
        }
    }
}
