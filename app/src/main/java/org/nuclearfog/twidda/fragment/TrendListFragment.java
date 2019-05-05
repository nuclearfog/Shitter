package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.AsyncTask.Status;
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

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.HomePagerAdapter.OnSettingsChanged;
import org.nuclearfog.twidda.adapter.OnItemClickListener;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.TrendLoader;
import org.nuclearfog.twidda.fragment.backend.TrendLoader.Mode;
import org.nuclearfog.twidda.window.SearchPage;


public class TrendListFragment extends Fragment implements OnRefreshListener, OnItemClickListener, OnSettingsChanged {

    private GlobalSettings settings;
    private TrendLoader trendTask;
    private SwipeRefreshLayout reload;
    private TrendAdapter adapter;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        super.onCreateView(inflater, parent, param);
        View v = inflater.inflate(R.layout.fragment_list, parent, false);

        settings = GlobalSettings.getInstance(getContext());
        adapter = new TrendAdapter(this);
        adapter.setColor(settings.getFontColor());

        reload = v.findViewById(R.id.fragment_reload);
        reload.setOnRefreshListener(this);
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());

        RecyclerView list = v.findViewById(R.id.fragment_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);
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
            trendTask = new TrendLoader(root, Mode.DB_TRND);
            trendTask.execute();
        }
    }


    @Override
    public void onStop() {
        if (trendTask != null && trendTask.getStatus() == Status.RUNNING)
            trendTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        trendTask = new TrendLoader(root, Mode.LD_TRND);
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


    @Override
    public void settingsChanged() {
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        adapter.setColor(settings.getFontColor());
        adapter.notifyDataSetChanged();
    }
}