package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.fragment.backend.TrendLoader;
import org.nuclearfog.twidda.window.SearchPage;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.fragment.backend.TrendLoader.Mode.DB_TRND;
import static org.nuclearfog.twidda.fragment.backend.TrendLoader.Mode.LD_TRND;


public class TrendListFragment extends Fragment implements OnRefreshListener, OnItemClickListener {

    private TrendLoader trendTask;
    private SwipeRefreshLayout reload;
    private TrendAdapter adapter;
    private ViewGroup root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        reload = new SwipeRefreshLayout(inflater.getContext());
        RecyclerView list = new RecyclerView(inflater.getContext());
        adapter = new TrendAdapter(this);
        list.setAdapter(adapter);
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle param) {
        super.onViewCreated(v,param);
        root = (ViewGroup) v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (trendTask == null) {
            trendTask = new TrendLoader(root, DB_TRND);
            trendTask.execute();
        }
    }


    @Override
    public void onStop() {
        if(trendTask != null && trendTask.getStatus() == RUNNING)
            trendTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        trendTask = new TrendLoader(root, LD_TRND);
        trendTask.execute();
    }


    @Override
    public void onItemClick(RecyclerView rv, int pos) {
        if (!reload.isRefreshing()) {
            String search = adapter.getData(pos).getName();
            Intent intent = new Intent(getContext(), SearchPage.class);
            if (!search.startsWith("#"))
                search = '\"' + search + '\"';
            intent.putExtra("search", search);
            startActivity(intent);
        }
    }
}