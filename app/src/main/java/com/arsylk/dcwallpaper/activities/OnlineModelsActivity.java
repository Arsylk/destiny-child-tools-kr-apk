package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;
import com.arsylk.dcwallpaper.Adapters.OnlineModelItem;
import com.arsylk.dcwallpaper.Adapters.OnlineModelsAdapter;
import com.arsylk.dcwallpaper.Async.AsyncWithDialog;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class OnlineModelsActivity extends AppCompatActivity {
    private Context context = OnlineModelsActivity.this;

    private AsyncWithDialog<Integer, Void, List<OnlineModelItem>> asyncLoading = null;
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
                            loadOnlineModels(adapter.getCount());
                        }
                    }
                }
            }
        });
        list_view.setAdapter(adapter);
        list_view.addFooterView(adapter.getLoaderView());
    }

    private void loadOnlineModels(int offset) {
        asyncLoading = new AsyncWithDialog<Integer, Void, List<OnlineModelItem>>(context, true, "Loading models...") {
            @Override
            protected List<OnlineModelItem> doInBackground(Integer... integers) {
                List<OnlineModelItem> onlineModels = new ArrayList<>();
                try {
                    String response = Jsoup.connect(String.format(Define.ONLINE_MODELS_URL, integers[0])).execute().body();
                    JSONObject json = new JSONObject(response);
                    JSONArray modelsJson = json.getJSONArray("models");
                    for(int i = 0; i < modelsJson.length(); i++) {
                        onlineModels.add(new OnlineModelItem(modelsJson.getJSONObject(i)));
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
                return onlineModels;
            }

            @Override
            protected void onPostExecute(List<OnlineModelItem> onlineModelItems) {
                super.onPostExecute(onlineModelItems);
                adapter.addItems(onlineModelItems);
            }
        };
        asyncLoading.execute(offset);
    }
}
