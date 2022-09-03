package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.ui.activities.TweetActivity;

import java.lang.ref.WeakReference;

/**
 * Background task to download tweet informations and to take actions
 *
 * @author nuclearfog
 * @see TweetActivity
 */
public class TweetAction extends AsyncTask<Long, Tweet, Void> {

	/**
	 * Load tweet
	 */
	public static final int LOAD = 1;

	/**
	 * load tweet from database first
	 */
	public static final int LD_DB = 2;

	/**
	 * retweet tweet
	 */
	public static final int RETWEET = 3;

	/**
	 * remove retweet
	 * (delete operation, "retweet ID" required)
	 */
	public static final int UNRETWEET = 4;

	/**
	 * favorite tweet
	 */
	public static final int FAVORITE = 5;

	/**
	 * remove tweet from favorites
	 */
	public static final int UNFAVORITE = 6;

	/**
	 * hide reply
	 */
	public static final int HIDE = 7;

	/**
	 * unhide reply
	 */
	public static final int UNHIDE = 8;

	/**
	 * delete own tweet
	 * (delete operation, "retweet ID" required)
	 */
	public static final int DELETE = 9;


	@Nullable
	private TwitterException twException;
	private Twitter twitter;
	private WeakReference<TweetActivity> weakRef;
	private AppDatabase db;
	private int action;

	/**
	 * @param action action for a given tweet
	 */
	public TweetAction(TweetActivity activity, int action) {
		super();
		weakRef = new WeakReference<>(activity);
		db = new AppDatabase(activity);
		twitter = Twitter.get(activity);

		this.action = action;
	}

	/**
	 * @param ids first value is the tweet ID. The second value is the retweet ID. Required for delete operations
	 */
	@Override
	protected Void doInBackground(Long... ids) {
		try {
			switch (action) {
				case LD_DB:
					Tweet newTweet = db.getTweet(ids[0]);
					if (newTweet != null) {
						publishProgress(newTweet);
					}
					// fall through

				case LOAD:
					newTweet = twitter.showTweet(ids[0]);
					//tweet = mTwitter.getStatus(tweetId);
					publishProgress(newTweet);
					if (db.containsTweet(ids[0])) {
						// update tweet if there is a database entry
						db.updateTweet(newTweet);
					}
					break;

				case DELETE:
					twitter.deleteTweet(ids[0]);
					db.removeTweet(ids[0]);
					// removing retweet reference to this tweet
					db.removeTweet(ids[1]);
					break;

				case RETWEET:
					newTweet = twitter.retweetTweet(ids[0]);
					if (newTweet.getEmbeddedTweet() != null)
						publishProgress(newTweet.getEmbeddedTweet());
					db.updateTweet(newTweet);
					break;

				case UNRETWEET:
					newTweet = twitter.unretweetTweet(ids[0]);
					publishProgress(newTweet);
					db.updateTweet(newTweet);
					// removing retweet reference to this tweet
					db.removeTweet(ids[1]);
					break;

				case FAVORITE:
					newTweet = twitter.favoriteTweet(ids[0]);
					publishProgress(newTweet);
					db.storeFavorite(newTweet);
					break;

				case UNFAVORITE:
					newTweet = twitter.unfavoriteTweet(ids[0]);
					publishProgress(newTweet);
					db.removeFavorite(newTweet);
					break;

				case HIDE:
					twitter.hideReply(ids[0], true);
					db.hideReply(ids[0], true);
					break;

				case UNHIDE:
					twitter.hideReply(ids[0], false);
					db.hideReply(ids[0], false);
					break;
			}
		} catch (TwitterException twException) {
			this.twException = twException;
			if (twException.getErrorType() == ErrorHandler.TwitterError.RESOURCE_NOT_FOUND) {
				// delete database entry if tweet was not found
				db.removeTweet(ids[0]);
				if (ids.length > 1) {
					// also remove reference to this tweet
					db.removeTweet(ids[1]);
				}
			}
		}
		return null;
	}


	@Override
	protected void onProgressUpdate(Tweet... tweets) {
		TweetActivity activity = weakRef.get();
		if (activity != null && tweets.length > 0 && tweets[0] != null) {
			activity.setTweet(tweets[0]);
		}
	}


	@Override
	protected void onPostExecute(Void v) {
		TweetActivity activity = weakRef.get();
		if (activity != null) {
			if (twException == null) {
				activity.OnSuccess(action);
			} else {
				activity.onError(twException);
			}
		}
	}
}