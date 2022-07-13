package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.MediaStream;
import org.nuclearfog.twidda.backend.api.holder.TweetUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.ui.activities.TweetEditor;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<TweetUpdate, Void, Void> {

	private Twitter twitter;
	private ErrorHandler.TwitterError twException;
	private WeakReference<TweetEditor> weakRef;

	/**
	 * initialize task
	 *
	 * @param activity Activity context
	 */
	public TweetUpdater(TweetEditor activity) {
		super();
		twitter = Twitter.get(activity);
		weakRef = new WeakReference<>(activity);
	}


	@Override
	protected Void doInBackground(TweetUpdate... tweets) {
		TweetUpdate update = tweets[0];
		try {
			// upload media first
			MediaStream[] mediaStreams = update.getMediaStreams();
			long[] mediaIds = new long[mediaStreams.length];
			for (int pos = 0; pos < mediaStreams.length; pos++) {
				// upload media file and save media ID
				mediaIds[pos] = twitter.uploadMedia(mediaStreams[pos]);
			}
			// upload tweet
			if (!isCancelled()) {
				twitter.uploadTweet(update, mediaIds);
			}
		} catch (TwitterException twException) {
			this.twException = twException;
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
			if (twException == null) {
				activity.onSuccess();
			} else {
				activity.onError(twException);
			}
		}
	}
}