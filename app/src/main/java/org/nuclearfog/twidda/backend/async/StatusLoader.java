package org.nuclearfog.twidda.backend.async;

import static org.nuclearfog.twidda.ui.fragments.StatusFragment.CLEAR_LIST;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.fragments.StatusFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Background task to download a list of tweets from different sources
 *
 * @author nuclearfog
 * @see StatusFragment
 */
public class StatusLoader extends AsyncTask<Long, Void, List<Status>> {

	/**
	 * tweets from home timeline
	 */
	public static final int HOME = 1;

	/**
	 * tweets from the mention timeline
	 */
	public static final int MENTION = 2;

	/**
	 * tweets of an user
	 */
	public static final int USER = 3;

	/**
	 * favorite tweets of an user
	 */
	public static final int FAVORIT = 4;

	/**
	 * tweet replies to a tweet
	 */
	public static final int REPLIES = 5;

	/**
	 * tweet replies from database
	 */
	public static final int REPLIES_OFFLINE = 6;

	/**
	 * tweets from twitter search
	 */
	public static final int SEARCH = 7;

	/**
	 * tweets from an userlist
	 */
	public static final int USERLIST = 8;

	private WeakReference<StatusFragment> weakRef;
	private Connection connection;
	private AppDatabase db;

	@Nullable
	private ConnectionException exception;
	private int listType;
	private String search;
	private long id;
	private int pos;

	/**
	 * @param fragment callback to update tweet data
	 * @param listType type of tweet list to load
	 * @param id       ID, depending on what list type should be loaded
	 * @param search   search string if any
	 * @param pos      index of the list where tweets should be inserted
	 */
	public StatusLoader(StatusFragment fragment, int listType, long id, String search, int pos) {
		super();
		db = new AppDatabase(fragment.getContext());
		connection = Twitter.get(fragment.getContext());
		weakRef = new WeakReference<>(fragment);

		this.listType = listType;
		this.search = search;
		this.id = id;
		this.pos = pos;
	}


	@Override
	protected List<org.nuclearfog.twidda.model.Status> doInBackground(Long[] param) {
		List<org.nuclearfog.twidda.model.Status> tweets = null;
		long sinceId = param[0];
		long maxId = param[1];
		try {
			switch (listType) {
				case HOME:
					if (sinceId == 0 && maxId == 0) {
						tweets = db.getHomeTimeline();
						if (tweets.isEmpty()) {
							tweets = connection.getHomeTimeline(sinceId, maxId);
							db.saveHomeTimeline(tweets);
						}
					} else if (sinceId > 0) {
						tweets = connection.getHomeTimeline(sinceId, maxId);
						db.saveHomeTimeline(tweets);
					} else if (maxId > 1) {
						tweets = connection.getHomeTimeline(sinceId, maxId);
					}
					break;

				case MENTION:
					if (sinceId == 0 && maxId == 0) {
						tweets = db.getMentionTimeline();
						if (tweets.isEmpty()) {
							tweets = connection.getMentionTimeline(sinceId, maxId);
							db.saveMentionTimeline(tweets);
						}
					} else if (sinceId > 0) {
						tweets = connection.getMentionTimeline(sinceId, maxId);
						db.saveMentionTimeline(tweets);
					} else if (maxId > 1) {
						tweets = connection.getMentionTimeline(sinceId, maxId);
					}
					break;

				case USER:
					if (id > 0) {
						if (sinceId == 0 && maxId == 0) {
							tweets = db.getUserTimeline(id);
							if (tweets.isEmpty()) {
								tweets = connection.getUserTimeline(id, 0, maxId);
								db.saveUserTimeline(tweets);
							}
						} else if (sinceId > 0) {
							tweets = connection.getUserTimeline(id, sinceId, maxId);
							db.saveUserTimeline(tweets);
						} else if (maxId > 1) {
							tweets = connection.getUserTimeline(id, sinceId, maxId);
						}
					} else if (search != null) {
						tweets = connection.getUserTimeline(search, sinceId, maxId);
					}
					break;

				case FAVORIT:
					if (id > 0) {
						if (sinceId == 0 && maxId == 0) {
							tweets = db.getUserFavorites(id);
							if (tweets.isEmpty()) {
								tweets = connection.getUserFavorits(id, 0, maxId);
								db.saveFavoriteTimeline(tweets, id);
							}
						} else if (sinceId > 0) {
							tweets = connection.getUserFavorits(id, 0, maxId);
							db.saveFavoriteTimeline(tweets, id);
							pos = CLEAR_LIST; // set flag to clear previous data
						} else if (maxId > 1) {
							tweets = connection.getUserFavorits(id, sinceId, maxId);
						}
					} else if (search != null) {
						tweets = connection.getUserFavorits(search, sinceId, maxId);
					}
					break;

				case REPLIES_OFFLINE:
					tweets = db.getReplies(id);
					break;

				case REPLIES:
					if (sinceId == 0 && maxId == 0) {
						tweets = db.getReplies(id);
						if (tweets.isEmpty()) {
							tweets = connection.getTweetReplies(search, id, sinceId, maxId);
							if (!tweets.isEmpty() && db.containsStatus(id)) {
								db.saveReplyTimeline(tweets);
							}
						}
					} else if (sinceId > 0) {
						tweets = connection.getTweetReplies(search, id, sinceId, maxId);
						if (!tweets.isEmpty() && db.containsStatus(id)) {
							db.saveReplyTimeline(tweets);
						}
					} else if (maxId > 1) {
						tweets = connection.getTweetReplies(search, id, sinceId, maxId);
					}
					break;

				case SEARCH:
					tweets = connection.searchStatuses(search, sinceId, maxId);
					break;

				case USERLIST:
					tweets = connection.getUserlistTweets(id, sinceId, maxId);
					break;
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return tweets;
	}


	@Override
	protected void onPostExecute(List<org.nuclearfog.twidda.model.Status> tweets) {
		StatusFragment fragment = weakRef.get();
		if (fragment != null) {
			if (tweets != null) {
				fragment.setData(tweets, pos);
			} else {
				fragment.onError(exception);
			}
		}
	}
}