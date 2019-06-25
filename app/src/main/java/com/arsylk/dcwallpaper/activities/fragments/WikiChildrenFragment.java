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
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import com.arsylk.dcwallpaper.Adapters.DCWikiChildrenAdapter;
import com.arsylk.dcwallpaper.R;

public class WikiChildrenFragment extends Fragment {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText searchInput;
    private ViewGroup searchStars;
    private DCWikiChildrenAdapter adapter;

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
        return inflater.inflate(R.layout.fragment_wiki_children, parent, false);
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
        drawerLayout = view.findViewById(R.id.wiki_fragment_children_drawer);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        // navigation UI
        navigationView = view.findViewById(R.id.wiki_fragment_children_navigation);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(!item.isChecked());
                item.getIcon().setAlpha(item.isChecked() ? 127 : 255);
                if(adapter != null)
                    adapter.toggleParameter(item.getItemId());
                return false;
            }
        });

        // children list UI dieplz
        ListView childrenList = view.findViewById(R.id.wiki_fragment_children_list);
        adapter = new DCWikiChildrenAdapter(getContext());
        adapter.cacheBitmaps();
        childrenList.setAdapter(adapter);

        // search input UI
        searchInput = navigationView.getHeaderView(0).findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adapter != null) {
                    adapter.getFilter().filter(searchInput.getText());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // star search UI
        searchStars = navigationView.getHeaderView(0).findViewById(R.id.search_stars_layout);
        for(int i = 0; i < searchStars.getChildCount(); i++) {
            searchStars.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(adapter != null) {
                        int stars = Integer.parseInt(view.getTag().toString());
                        for(int i = 1; i < searchStars.getChildCount(); i++) {
                            searchStars.getChildAt(i).setAlpha((i <= stars) ? 1.0f : 0.5f);
                        }
                        adapter.toggleStars(stars);
                    }
                }
            });
        }
    }

}
