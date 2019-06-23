package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

import static android.os.AsyncTask.Status.FINISHED;


public class TrendLoader extends AsyncTask<Void, Void, List<String>> {

    private WeakReference<View> ui;
    private TwitterException err;
    private TwitterEngine mTwitter;
    private DatabaseAdapter db;
    private TrendAdapter adapter;
    private int woeId;


    public TrendLoader(@NonNull View root) {
        ui = new WeakReference<>(root);
        mTwitter = TwitterEngine.getInstance(root.getContext());
        db = new DatabaseAdapter(root.getContext());
        GlobalSettings settings = GlobalSettings.getInstance(root.getContext());
        RecyclerView list = root.findViewById(R.id.fragment_list);
        adapter = (TrendAdapter) list.getAdapter();
        woeId = settings.getTrendLocation().getWoeId();
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null)
            return;
        final SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getStatus() != FINISHED)
                    reload.setRefreshing(true);
            }
        }, 500);
    }


    @Override
    protected List<String> doInBackground(Void[] v) {
        List<String> trends = null;
        try {
            if (adapter.isEmpty()) {
                trends = db.getTrends(woeId);
                if (trends.isEmpty()) {
                    trends = mTwitter.getTrends(woeId);
                    db.storeTrends(trends, woeId);
                }
            } else {
                trends = mTwitter.getTrends(woeId);
                db.storeTrends(trends, woeId);
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return trends;
    }


    @Override
    protected void onPostExecute(@Nullable List<String> trends) {
        if (ui.get() != null) {
            if (trends != null)
                adapter.setData(trends);
            if (err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null) {
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled(@Nullable List<String> trends) {
        if (ui.get() != null) {
            if (trends != null)
                adapter.setData(trends);
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }
}