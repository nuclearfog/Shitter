package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.ui.activities.MetricsActivity;

import java.lang.ref.WeakReference;

/**
 * background asynctask to fetch tweet metrics
 *
 * @author nuclearfog
 */
public class MetricsLoader extends AsyncTask<Long, Void, Metrics> {

	private WeakReference<MetricsActivity> callback;
	private Connection connection;

	@Nullable
	private ConnectionException exception;


	public MetricsLoader(MetricsActivity activity) {
		super();
		callback = new WeakReference<>(activity);
		connection = Twitter.get(activity);
	}


	@Override
	protected Metrics doInBackground(Long[] ids) {
		try {
			return connection.getTweetMetrics(ids[0]);
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(Metrics metrics) {
		MetricsActivity activity = callback.get();
		if (activity != null) {
			if (metrics != null) {
				activity.onSuccess(metrics);
			} else {
				activity.onError(exception);
			}
		}
	}
}