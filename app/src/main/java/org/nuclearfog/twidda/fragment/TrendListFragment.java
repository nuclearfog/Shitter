package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.FragmentAdapter.FragmentChangeObserver;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.backend.TrendLoader;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.window.SearchPage;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH_QUERY;


public class TrendListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, FragmentChangeObserver {

    private TrendLoader trendTask;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TrendAdapter adapter;
    private GlobalSettings settings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        list = v.findViewById(R.id.fragment_list);
        reload = v.findViewById(R.id.fragment_reload);

        settings = GlobalSettings.getInstance(getContext());
        reload.setOnRefreshListener(this);
        adapter = new TrendAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        setColors();
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (trendTask == null)
            load();
    }


    @Override
    public void onDestroy() {
        if (trendTask != null && trendTask.getStatus() == RUNNING)
            trendTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (trendTask != null && trendTask.getStatus() != RUNNING)
            load();
    }


    @Override
    public void onItemClick(int pos) {
        if (!reload.isRefreshing()) {
            String search = adapter.getData(pos);
            Intent intent = new Intent(getContext(), SearchPage.class);
            if (!search.startsWith("#"))
                search = '\"' + search + '\"';
            intent.putExtra(KEY_SEARCH_QUERY, search);
            startActivity(intent);
        }
    }


    @Override
    public void onSettingsChange() {
        if (adapter != null && reload != null) {
            adapter.clear();
            setColors();
        }
        trendTask = null;
    }


    @Override
    public void onTabChange() {
        if (list != null)
            list.smoothScrollToPosition(0);
    }


    @Override
    public void onDataClear() {
        if (adapter != null)
            adapter.clear();
        trendTask = null;
    }


    public TrendAdapter getAdapter() {
        return adapter;
    }


    public void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (trendTask.getStatus() != FINISHED && !reload.isRefreshing())
                        reload.setRefreshing(true);
                }
            }, 500);
        } else {
            reload.setRefreshing(false);
        }
    }


    private void load() {
        trendTask = new TrendLoader(this);
        trendTask.execute();
    }


    private void setColors() {
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getFontColor());
    }
}