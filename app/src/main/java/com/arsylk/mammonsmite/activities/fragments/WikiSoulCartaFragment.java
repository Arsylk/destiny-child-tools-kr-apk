package com.arsylk.mammonsmite.activities.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.arsylk.mammonsmite.Adapters.DCWikiSoulCartaAdapter;
import com.arsylk.mammonsmite.R;

public class WikiSoulCartaFragment extends Fragment {
    private DCWikiSoulCartaAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wiki_soul_carta, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView soulCartaList = view.findViewById(R.id.wiki_soul_carta_list);
        soulCartaList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));
        adapter = new DCWikiSoulCartaAdapter(getContext());
        soulCartaList.setAdapter(adapter);
    }
}
