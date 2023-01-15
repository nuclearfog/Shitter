package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
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
public class TrendLoader extends AsyncTask<String, Void, List<Trend>> {

	private WeakReference<TrendFragment> weakRef;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private boolean isEmpty;

	/**
	 * @param fragment callback to update data
	 */
	public TrendLoader(TrendFragment fragment) {
		super();
		weakRef = new WeakReference<>(fragment);
		db = new AppDatabase(fragment.getContext());
		connection = ConnectionManager.get(fragment.getContext());
		isEmpty = fragment.isEmpty();
	}


	@Override
	protected List<Trend> doInBackground(String... params) {
		List<Trend> trends = null;
		try {
			if (params.length == 1 && params[0] != null && !params[0].trim().isEmpty()) {
				trends = connection.searchHashtags(params[0]);
			} else if (isEmpty) {
				trends = db.getTrends();
				if (trends.isEmpty()) {
					trends = connection.getTrends();
					db.saveTrends(trends);
				}
			} else {
				trends = connection.getTrends();
				db.saveTrends(trends);
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trends;
	}


	@Override
	protected void onPostExecute(@Nullable List<Trend> trends) {
		TrendFragment fragment = weakRef.get();
		if (fragment != null) {
			if (trends != null) {
				fragment.setData(trends);
			}
			if (trends == null || exception != null) {
				fragment.onError(exception);
			}
		}
	}
}