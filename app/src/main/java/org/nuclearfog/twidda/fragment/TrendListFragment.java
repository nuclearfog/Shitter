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
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.TrendLoader;
import org.nuclearfog.twidda.window.SearchPage;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH;


public class TrendListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, FragmentChangeObserver {

    private TrendLoader trendTask;
    private SwipeRefreshLayout reload;
    private RecyclerView list;
    private TrendAdapter adapter;
    private GlobalSettings settings;
    private View root;

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
    public void onViewCreated(@NonNull View v, Bundle param) {
        super.onViewCreated(v, param);
        root = v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (trendTask == null) {
            trendTask = new TrendLoader(root);
            trendTask.execute();
        }
    }


    @Override
    public void onStop() {
        if (trendTask != null && trendTask.getStatus() == RUNNING)
            trendTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        trendTask = new TrendLoader(root);
        trendTask.execute();
    }


    @Override
    public void onItemClick(int pos) {
        if (!reload.isRefreshing()) {
            String search = adapter.getData(pos).getName();
            Intent intent = new Intent(getContext(), SearchPage.class);
            if (!search.startsWith("#"))
                search = '\"' + search + '\"';
            intent.putExtra(KEY_SEARCH, search);
            startActivity(intent);
        }
    }


    @Override
    public void onSettingsChange() {
        setColors();
        adapter.clear();
        trendTask = null;
    }


    @Override
    public void onTabChange() {
        if (list != null)
            list.smoothScrollToPosition(0);
    }


    @Override
    public void onDataClear() {
        adapter.clear();
        trendTask = null;
    }


    private void setColors() {
        if (reload != null)
            reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getFontColor());
    }
}