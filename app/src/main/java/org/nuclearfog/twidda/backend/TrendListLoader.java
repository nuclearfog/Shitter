package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterTrend;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TrendFragment;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Background task to load a list of location specific trends
 *
 * @see TrendFragment
 */
public class TrendListLoader extends AsyncTask<Integer, Void, List<TwitterTrend>> {

    @Nullable
    private EngineException twException;
    private final WeakReference<TrendFragment> callback;
    private final TwitterEngine mTwitter;
    private final AppDatabase db;
    private final boolean isEmpty;

    /**
     * @param callback callback to update data
     */
    public TrendListLoader(TrendFragment callback) {
        super();
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        isEmpty = callback.isEmpty();
    }


    @Override
    protected List<TwitterTrend> doInBackground(Integer[] param) {
        List<TwitterTrend> trends;
        int woeId = param[0];
        try {
            if (isEmpty) {
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
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<TwitterTrend> trends) {
        if (callback.get() != null) {
            if (trends != null) {
                callback.get().setData(trends);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}