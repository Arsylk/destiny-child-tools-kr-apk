package com.arsylk.dcwallpaper.activities.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.arsylk.dcwallpaper.Adapters.DCWikiEquipmentAdapter;
import com.arsylk.dcwallpaper.R;

public class WikiEquipmentFragment extends Fragment {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText navigationSearchInput;
    private DCWikiEquipmentAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wiki_equipment, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // parent toolbar UI
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if(appCompatActivity != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            }
        }

        // drawer layout UI
        drawerLayout = view.findViewById(R.id.wiki_fragment_equipment_drawer);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        // navigation UI
        navigationView = view.findViewById(R.id.wiki_fragment_equipment_navigation);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(!item.isChecked());
                if(item.getIcon() != null)
                    item.getIcon().setAlpha(item.isChecked() ? 127 : 255);
                if(adapter != null)
                    adapter.toggleParameter(item.getItemId());
                return false;
            }
        });

        // navigation search UI
        navigationSearchInput = navigationView.getHeaderView(0).findViewById(R.id.search_input);
        navigationSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adapter != null) {
                    adapter.getFilter().filter(navigationSearchInput.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        navigationView.getHeaderView(0).findViewById(R.id.search_stars_layout).setVisibility(View.GONE);

        // equipment list UI
        ListView equipmentList = view.findViewById(R.id.wiki_fragment_equipment_list);
        adapter = new DCWikiEquipmentAdapter(getContext());
        equipmentList.setAdapter(adapter);
    }
}
