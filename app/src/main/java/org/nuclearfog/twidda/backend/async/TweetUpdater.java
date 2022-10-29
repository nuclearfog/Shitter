package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.update.MediaUpdate;
import org.nuclearfog.twidda.backend.api.twitter.update.TweetUpdate;
import org.nuclearfog.twidda.ui.activities.TweetEditor;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<TweetUpdate, Void, Void> {

	private Connection connection;
	private ConnectionException exception;
	private WeakReference<TweetEditor> weakRef;

	/**
	 * initialize task
	 *
	 * @param activity Activity context
	 */
	public TweetUpdater(TweetEditor activity) {
		super();
		connection = Twitter.get(activity);
		weakRef = new WeakReference<>(activity);
	}


	@Override
	protected Void doInBackground(TweetUpdate... tweets) {
		TweetUpdate update = tweets[0];
		try {
			// upload media first
			MediaUpdate[] mediaUpdates = update.getMediaUpdates();
			long[] mediaIds = new long[mediaUpdates.length];
			for (int pos = 0; pos < mediaUpdates.length; pos++) {
				// upload media file and save media ID
				mediaIds[pos] = connection.uploadMedia(mediaUpdates[pos]);
			}
			// upload tweet
			if (!isCancelled()) {
				connection.uploadTweet(update, mediaIds);
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} finally {
			// close inputstreams
			update.close();
		}
		return null;
	}


	@Override
	protected void onPostExecute(Void v) {
		TweetEditor activity = weakRef.get();
		if (activity != null) {
			if (exception == null) {
				activity.onSuccess();
			} else {
				activity.onError(exception);
			}
		}
	}
}