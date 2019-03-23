package com.arsylk.dcwallpaper.Adapters;

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
import com.arsylk.dcwallpaper.Async.interfaces.OnUnpackFinishedListener;
import com.arsylk.dcwallpaper.DestinyChild.DCModel;
import com.arsylk.dcwallpaper.DestinyChild.DCTools;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.activities.DCModelsActivity;
import com.arsylk.dcwallpaper.utils.Define;
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

    //constructors
    public OnlineModelsAdapter(Context context) {
        this.context = context;
        this.onlineModels = new ArrayList<>();
    }

    public OnlineModelsAdapter(Context context, List<OnlineModelItem> onlineModels) {
        this.context = context;
        this.onlineModels = onlineModels;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.item_online_model, parent, false);

        final OnlineModelItem onlineModel = onlineModels.get(position);
        final TextView mod_title, mod_creator, mod_description, model_region, model_id;
        final ImageView image_preview;
        try {
            //metadata layout
            mod_title = convertView.findViewById(R.id.mod_tile);
            mod_creator = convertView.findViewById(R.id.mod_creator);
            mod_description = convertView.findViewById(R.id.mod_description);

            model_region = convertView.findViewById(R.id.model_region);
            model_id = convertView.findViewById(R.id.model_id);

            mod_title.setText(onlineModel.getModTitle());
            mod_creator.setText(String.format("by %s", onlineModel.getModCreator()));
            mod_description.setText(onlineModel.getModDescription());

            model_region.setText(onlineModel.getModelRegion());
            model_id.setText(onlineModel.getModelId());

            convertView.findViewById(R.id.layout_mod_metadata).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Downloading...");
                    progressDialog.show();
                    Ion.with(context).load(onlineModel.getFileModel())
                            .progressDialog(progressDialog)
                            .asInputStream().setCallback(new FutureCallback<InputStream>() {
                        @Override
                        public void onCompleted(Exception e, InputStream in) {
                            progressDialog.dismiss();
                            if(e == null) {
                                onFileLoaded(onlineModel, in);
                            }else {
                                Toast.makeText(context, "Failed to download!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            //image layout
            image_preview = convertView.findViewById(R.id.image_preview);

            Ion.with(context).load(onlineModel.getFilePreview())
                    .asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap bitmap) {
                    if(e == null) {
                        onBitmapLoaded(image_preview, bitmap);
                    } else {
                        image_preview.setImageResource(R.drawable.ic_error_outline_black);
                    }
                }
            });
        }catch(Exception e) {
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
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomPopup.dismiss();
                        dimPopup.dismiss();
                    }
                };
                dimPopup.getContentView().setOnClickListener(onClickListener);
                zoomPopup.getContentView().setOnClickListener(onClickListener);

                dimPopup.showAtLocation(dimPopup.getContentView(), Gravity.CENTER, 0, 0);
                zoomPopup.showAtLocation(zoomPopup.getContentView(), Gravity.CENTER, 0, 0);
            }
        });
    }

    private void onFileLoaded(OnlineModelItem modelItem, InputStream in) {
        try {
            File file = new File(Define.ONLINE_DIRECTORY,
                    modelItem.getModelId()+"."+modelItem.getModCreator().toLowerCase()+".pck");
            FileUtils.copyInputStreamToFile(in, file);
            DCTools.asyncUnpack(file, context, new OnUnpackFinishedListener() {
                @Override
                public void onFinished(DCModel dcModel) {
                    if(dcModel != null) {
                        DCModelsActivity.showPickAction(context, dcModel);
                    }else {
                        Toast.makeText(context, "Failed to unpack!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //setters & getters
    @Override
    public int getCount() {
        return onlineModels.size();
    }

    public void addItem(OnlineModelItem modelItem) {
        onlineModels.add(modelItem);
        notifyDataSetChanged();
    }

    @Override
    public OnlineModelItem getItem(int position) {
        return onlineModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}

