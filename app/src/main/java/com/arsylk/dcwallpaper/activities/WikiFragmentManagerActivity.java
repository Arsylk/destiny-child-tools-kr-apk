package com.arsylk.dcwallpaper.activities;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.arsylk.dcwallpaper.R;
import com.arsylk.dcwallpaper.activities.fragments.WikiChildrenFragment;
import com.arsylk.dcwallpaper.activities.fragments.WikiEquipmentFragment;

public class WikiFragmentManagerActivity extends ActivityWithExceptionRedirect {
    private Context context = WikiFragmentManagerActivity.this;
    private WikiChildrenFragment childrenFragment;
    private WikiEquipmentFragment equipmentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki_fragment_manager);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wiki_fragments_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.wiki_menu_children:
                setFragment(childrenFragment);
                return true;
            case R.id.wiki_menu_equipment:
                setFragment(equipmentFragment);
            case R.id.wiki_menu_soul_carta:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        // initialize fragments
        childrenFragment = new WikiChildrenFragment();
        equipmentFragment = new WikiEquipmentFragment();

        // set default fragment
        setFragment(equipmentFragment);
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wiki_fragment_placeholder, fragment)
                .addToBackStack(null)
                .commit();
    }
}
