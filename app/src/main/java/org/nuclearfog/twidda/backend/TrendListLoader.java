package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TrendFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


/**
 * Background task to load a list of location specific trends
 *
 * @see TrendFragment
 */
public class TrendListLoader extends AsyncTask<Integer, Void, List<TwitterTrend>> {

    @Nullable
    private TwitterEngine.EngineException twException;
    private WeakReference<TrendFragment> ui;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private TrendAdapter adapter;


    public TrendListLoader(@NonNull TrendFragment fragment) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<TwitterTrend> doInBackground(Integer[] param) {
        List<TwitterTrend> trends;
        int woeId = param[0];
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
            return trends;
        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable List<TwitterTrend> trends) {
        if (ui.get() != null) {
            if (trends != null)
                adapter.setData(trends);
            if (twException != null)
                Toast.makeText(ui.get().getContext(), twException.getMessageResource(), LENGTH_SHORT).show();
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }
}