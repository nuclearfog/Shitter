package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Trend;
import org.nuclearfog.twidda.ui.fragments.TrendFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to load a list of location specific trends
 *
 * @author nuclearfog
 * @see TrendFragment
 */
public class TrendLoader extends AsyncTask<Integer, Void, List<Trend>> {

    @Nullable
    private TwitterException twException;
    private WeakReference<TrendFragment> weakRef;
    private Twitter twitter;
    private AppDatabase db;
    private boolean isEmpty;

    /**
     * @param fragment callback to update data
     */
    public TrendLoader(TrendFragment fragment) {
        super();
        weakRef = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        twitter = Twitter.get(fragment.getContext());
        isEmpty = fragment.isEmpty();
    }


    @Override
    protected List<Trend> doInBackground(Integer[] param) {
        List<Trend> trends;
        int woeId = param[0];
        try {
            if (isEmpty) {
                trends = db.getTrends(woeId);
                if (trends.isEmpty()) {
                    trends = twitter.getTrends(woeId);
                    db.storeTrends(trends, woeId);
                }
            } else {
                trends = twitter.getTrends(woeId);
                db.storeTrends(trends, woeId);
            }
            return trends;
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<Trend> trends) {
        TrendFragment fragment = weakRef.get();
        if (fragment != null) {
            if (trends != null) {
                fragment.setData(trends);
            } else {
                fragment.onError(twException);
            }
        }
    }
}