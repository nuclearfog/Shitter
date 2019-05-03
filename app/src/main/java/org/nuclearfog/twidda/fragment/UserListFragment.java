package org.nuclearfog.twidda.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.adapter.OnItemClickListener;

public class UserListFragment extends Fragment implements OnRefreshListener, OnItemClickListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        SwipeRefreshLayout reload = new SwipeRefreshLayout(inflater.getContext());
        RecyclerView list = new RecyclerView(inflater.getContext());
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onRefresh() {

    }


    @Override
    public void onItemClick(RecyclerView rv, int pos) {

    }
}