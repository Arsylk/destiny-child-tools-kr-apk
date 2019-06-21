package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.OnlineModelItem;
import com.arsylk.dcwallpaper.Adapters.OnlineModelsAdapter;
import com.arsylk.dcwallpaper.Async.AsyncOnlineModels;
import com.arsylk.dcwallpaper.Async.AsyncWithDialog;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.arsylk.dcwallpaper.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class OnlineModelsActivity extends ActivityWithExceptionRedirect {
    private Context context = OnlineModelsActivity.this;

    private AsyncOnlineModels asyncLoading = null;
    private ListView list_view;
    private OnlineModelsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_models);
        initViews();
        loadOnlineModels(0);
    }

    private void initViews() {
        adapter = new OnlineModelsAdapter(context);

        list_view = findViewById(R.id.online_models_list);
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if((view.getLastVisiblePosition() == adapter.getCount())) {
                    if(asyncLoading != null) {
                        if(asyncLoading.getStatus() == AsyncTask.Status.FINISHED) {
                            if(!adapter.isFullyLoaded()) {
                                loadOnlineModels(adapter.getCount());
                            }
                        }
                    }
                }
            }
        });
        list_view.setAdapter(adapter);
        list_view.addFooterView(adapter.getLoaderView());
    }

    private void loadOnlineModels(int offset) {
        // only one task running
        if(asyncLoading != null) {
            if(asyncLoading.getStatus() != AsyncTask.Status.FINISHED) {
                Toast.makeText(context, "Models still loading...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // load online models
        asyncLoading = new AsyncOnlineModels(context, false);
        asyncLoading.setOnProgressUpdate(new Utils.OnProgressUpdate<OnlineModelItem>() {
            @Override
            public void onProgressUpdate(OnlineModelItem item) {
                adapter.addItem(item);
            }
        });
        asyncLoading.setOnPostExecute(new Utils.OnPostExecute<Boolean>() {
            @Override
            public void onPostExecute(Boolean item) {
                adapter.setFullyLoaded(!item);
            }
        });
        asyncLoading.execute(offset);
    }
}
