package org.nuclearfog.twidda.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * this fragment class hosts a list view inside a swipe view
 * superclass for all list fragments
 *
 * @author nuclearfog
 */
public abstract class ListFragment extends Fragment implements OnRefreshListener {

    /**
     * delay to enable swipe view in milliseconds
     */
    private static final int REFRESH_DELAY = 500;

    private RecyclerView list;
    private SwipeRefreshLayout reload;
    protected GlobalSettings settings;

    private boolean refreshLock = false;


    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle b) {
        settings = GlobalSettings.getInstance(requireContext());
        onCreate();

        list = new RecyclerView(requireContext());
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(initAdapter());

        reload = new SwipeRefreshLayout(requireContext());
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public final void onRefresh() {
        refreshLock = true;
        onReload();
    }

    /**
     * enables or disables swipe layout
     *
     * @param enable true to enable swipe view delayed, false to stop immediately
     */
    protected void setRefresh(boolean enable) {
        refreshLock = !enable;
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!reload.isRefreshing() && !refreshLock) {
                        reload.setRefreshing(true);
                    }
                }
            }, REFRESH_DELAY);
        } else {
            reload.setRefreshing(false);
        }
    }

    /**
     * check if swipe refresh is active
     *
     * @return true if swipe view is active
     */
    protected boolean isRefreshing() {
        return reload.isRefreshing();
    }

    /**
     * called to reset all data
     */
    public void reset() {
        // check if fragment is initialized
        if (reload != null && list != null) {
            reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
            list.setAdapter(list.getAdapter()); // force redrawing list to apply colors
            onReset();
        }
    }

    /**
     * called when this tab is deselected
     */
    public void onTabChange() {
        if (list != null) {
            list.smoothScrollToPosition(0);
        }
    }

    /**
     * called to initialize sub classes
     */
    protected abstract void onCreate();

    /**
     * called when swipe refresh is active
     */
    protected abstract void onReload();

    /**
     * called to reset all data
     */
    protected abstract void onReset();

    /**
     * initialize list adapter
     *
     * @return adapter for the recycler view list
     */
    protected abstract Adapter<? extends ViewHolder> initAdapter();
}