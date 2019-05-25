package com.arsylk.dcwallpaper.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.arsylk.dcwallpaper.Adapters.OnlineModelItem;
import com.arsylk.dcwallpaper.Adapters.OnlineModelsAdapter;
import com.arsylk.dcwallpaper.Async.AsyncWithDialog;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.utils.Define;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class OnlineModelsActivity extends AppCompatActivity {
    private Context context = OnlineModelsActivity.this;
    private ProgressDialog progressDialog;

    private ListView list_view;
    private OnlineModelsAdapter adapter;
    private List<OnlineModelItem> onlineModelItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_models);
        initViews();
        loadViews();
    }

    private void initViews() {
        list_view = findViewById(R.id.online_models_list);
        adapter = new OnlineModelsAdapter(context);
        list_view.setAdapter(adapter);
    }

    private void loadViews() {
        new AsyncWithDialog<Void, Void, List<OnlineModelItem>>(context, true, "Loading models...") {
            @Override
            protected List<OnlineModelItem> doInBackground(Void... voids) {
                List<OnlineModelItem> onlineModels = new ArrayList<>();
                try {
                    String response = Jsoup.connect(Define.ONLINE_MODELS_URL).execute().body();
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
        }.execute();
//        onlineModelItems = new ArrayList<>();
//        Ion.with(context).load(Define.ONLINE_MODELS_URL)
//                .progressDialog(progressDialog)
//                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
//            @Override
//            public void onCompleted(Exception e, JsonObject json) {
//                progressDialog.dismiss();
//                if(json != null) {
//                    JsonArray jsonArrayModels = json.getAsJsonArray("models");
//                    for(int i = 0; i < jsonArrayModels.size(); i++) {
//                        JsonObject jsonModel = jsonArrayModels.get(i).getAsJsonObject();
//                        onlineModelItems.add(new OnlineModelItem(jsonModel));
//                        adapter.addItem(onlineModelItems.get(i));
//                    }
//                }else {
//                    Toast.makeText(context, "Failed to fileLoad!", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//            }
//        });
    }
}
