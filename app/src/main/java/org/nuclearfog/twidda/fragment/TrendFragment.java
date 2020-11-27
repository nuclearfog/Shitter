package org.nuclearfog.twidda.fragment;

import android.content.Intent;

import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.adapter.TrendAdapter.TrendClickListener;
import org.nuclearfog.twidda.backend.TrendListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;

/**
 * Fragment class for trend lists
 */
public class TrendFragment extends ListFragment implements TrendClickListener {


    private TrendListLoader trendTask;
    private TrendAdapter adapter;


    @Override
    protected void onCreate() {
        settings = GlobalSettings.getInstance(requireContext());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (trendTask == null) {
            load();
            setRefresh(true);
        }
    }


    @Override
    protected void onReset() {
        adapter.clear();
        load();
        setRefresh(true);
    }


    @Override
    public void onDestroy() {
        if (trendTask != null && trendTask.getStatus() == RUNNING)
            trendTask.cancel(true);
        super.onDestroy();
    }


    @Override
    protected TrendAdapter initAdapter() {
        adapter = new TrendAdapter(this, settings);
        return adapter;
    }


    @Override
    protected void onReload() {
        if (trendTask != null && trendTask.getStatus() != RUNNING) {
            load();
        }
    }


    @Override
    public void onTrendClick(TwitterTrend trend) {
        if (!isRefreshing()) {
            Intent intent = new Intent(requireContext(), SearchPage.class);
            intent.putExtra(KEY_SEARCH_QUERY, trend.getSearchString());
            startActivity(intent);
        }
    }

    /**
     * set trend data to list
     *
     * @param data Trend data
     */
    public void setData(List<TwitterTrend> data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * check if list is empty
     *
     * @return true if list is empty
     */
    public boolean isEmpty() {
        return adapter.isEmpty();
    }

    /**
     * called from {@link TrendListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        if (error != null)
            ErrorHandler.handleFailure(requireContext(), error);
        setRefresh(false);
    }

    /**
     * load content into the list
     */
    private void load() {
        trendTask = new TrendListLoader(this);
        trendTask.execute(settings.getTrendLocation().getWoeId());
    }
}