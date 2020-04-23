package com.arsylk.mammonsmite.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arsylk.mammonsmite.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.mammonsmite.DestinyChild.DCModel;
import com.arsylk.mammonsmite.DestinyChild.DCTools;
import com.arsylk.mammonsmite.Live2D.L2DModel;
import com.arsylk.mammonsmite.R;
import com.arsylk.mammonsmite.activities.DCModelsActivity;
import com.arsylk.mammonsmite.utils.Define;
import com.arsylk.mammonsmite.utils.Log;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OnlineModelsAdapter extends BaseAdapter {
    private Context context;
    private List<OnlineModelItem> onlineModels;
    private View loaderView = null;
    private boolean fullyLoaded = false;

    //constructors
    public OnlineModelsAdapter(Context context) {
        this.context = context;
        this.onlineModels = new ArrayList<>();
    }

    public View getLoaderView() {
        if(loaderView == null) {
            loaderView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.footer_progress, null);
            loaderView.findViewById(R.id.footer_progressbar).setVisibility(View.VISIBLE);
            loaderView.setClickable(false);
            loaderView.setEnabled(false);
        }
        return loaderView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.item_online_model, parent, false);

        final OnlineModelItem onlineModel = onlineModels.get(position);
        final TextView mod_title, mod_creator, mod_description, model_id;
        final ImageView image_preview;
        try {
            //metadata layout
            model_id = convertView.findViewById(R.id.model_id);
            mod_title = convertView.findViewById(R.id.mod_tile);
            mod_creator = convertView.findViewById(R.id.mod_creator);
            mod_description = convertView.findViewById(R.id.mod_description);


            model_id.setText(onlineModel.getModelId());
            mod_title.setText(onlineModel.getModelName());
            mod_creator.setText(String.format("by %s", onlineModel.getCreator()));
            mod_description.setText(onlineModel.getDescription());


            convertView.findViewById(R.id.layout_mod_metadata).setOnClickListener(v -> {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Downloading...");
                progressDialog.show();
                Ion.with(context).load(onlineModel.getModelUrl()).setTimeout(0)
                        .progressDialog(progressDialog)
                        .asInputStream().setCallback((e, in) -> {
                            progressDialog.dismiss();
                            if(e == null) {
                                onFileLoaded(onlineModel, in);
                            }else {
                                e.printStackTrace();
                                Toast.makeText(context, "Failed to download!", Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            // image layout
            image_preview = convertView.findViewById(R.id.image_preview);
            if(onlineModel.getPreviewBitmap() != null) {
                // handle zoom on click
                onBitmapLoaded(image_preview, onlineModel.getPreviewBitmap());
            }
        }catch(Exception e) {
            Log.append(e.getClass().getSimpleName(), e.toString());
            e.printStackTrace();
        }

        return convertView;
    }

    private void onBitmapLoaded(ImageView imageView, final Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

                final PopupWindow dimPopup = new PopupWindow(new View(context), metrics.widthPixels, metrics.heightPixels, true);
                dimPopup.setBackgroundDrawable(new ColorDrawable(0xa0000000));

                ImageView zoomImageView = new ImageView(context);
                zoomImageView.setImageBitmap(bitmap);
                final PopupWindow zoomPopup = new PopupWindow(zoomImageView, (int) (metrics.widthPixels * 0.8), (int) (metrics.heightPixels * 0.6), false);

                //dismiss if any window clicked
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomPopup.dismiss();
                        dimPopup.dismiss();
                    }
                };
                dimPopup.getContentView().setOnClickListener(onClickListener);
                zoomPopup.getContentView().setOnClickListener(onClickListener);

                //dismiss if any window disappeared
                PopupWindow.OnDismissListener onDismiss = new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        zoomPopup.dismiss();
                        dimPopup.dismiss();
                    }
                };
                dimPopup.setOnDismissListener(onDismiss);
                zoomPopup.setOnDismissListener(onDismiss);

                dimPopup.showAtLocation(dimPopup.getContentView(), Gravity.CENTER, 0, 0);
                zoomPopup.showAtLocation(zoomPopup.getContentView(), Gravity.CENTER, 0, 0);
            }
        });
    }

    private void onFileLoaded(final OnlineModelItem modelItem, InputStream in) {
        try {
            //save file
            File downloadedFile = new File(Define.ONLINE_DIRECTORY,modelItem.getModelId()+"."+modelItem.getCreator().toLowerCase()+".pck");
            FileUtils.copyInputStreamToFile(in, downloadedFile);
            DCTools.asyncUnpack(downloadedFile, context, dcModel -> {
                if(dcModel != null) {
                    if(dcModel.isLoaded()) {
                        L2DModel l2DModel = dcModel.asL2DModel();
                        l2DModel.setModelInfoJson(modelItem.getModelInfo());
                        DCModelsActivity.showPickAction(context, l2DModel);
                    }
                }else {
                    Toast.makeText(context, "Failed to unpack!", Toast.LENGTH_SHORT).show();
                }
            });
        }catch(Exception e) {
            Log.append(e.getClass().getSimpleName(), e.toString());
            e.printStackTrace();
        }
    }

    //setters & getters
    public void addItem(OnlineModelItem modelItem) {
        onlineModels.add(modelItem);
        notifyDataSetChanged();
        Log.append(OnlineModelsAdapter.class.getSimpleName(), "addItem: "+modelItem.toString());
    }

    @Override
    public OnlineModelItem getItem(int position) {
        return onlineModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return onlineModels.get(position).getId();
    }

    @Override
    public int getCount() {
        return onlineModels.size();
    }

    public void setFullyLoaded(boolean fullyLoaded) {
        // hide footer if created
        if(loaderView != null) {
            loaderView.findViewById(R.id.footer_progressbar).setVisibility(fullyLoaded ? View.GONE : View.VISIBLE);
        }
        this.fullyLoaded = fullyLoaded;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
}

