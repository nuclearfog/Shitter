package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.adapter.TweetAdapter.TweetClickListener;
import org.nuclearfog.twidda.backend.async.TweetLoader;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.ui.activities.TweetActivity;

import java.util.List;

/**
 * fragment class to show a list of tweets
 *
 * @author nuclearfog
 */
public class TweetFragment extends ListFragment implements TweetClickListener {

	/**
	 * Key to define what type of tweets should be loaded
	 * possible values are {@link #TWEET_FRAG_HOME,#TWEET_FRAG_MENT,#TWEET_FRAG_TWEETS,#TWEET_FRAG_FAVORS,#TWEET_FRAG_ANSWER,#TWEET_FRAG_SEARCH,#TWEET_FRAG_LIST}
	 */
	public static final String KEY_FRAG_TWEET_MODE = "tweet_mode";

	/**
	 * Key to define a search query
	 * value type is String
	 */
	public static final String KEY_FRAG_TWEET_SEARCH = "tweet_search";

	/**
	 * Key to define a an (tweet, user, list) ID
	 * value type is Long
	 */
	public static final String KEY_FRAG_TWEET_ID = "tweet_id";

	/**
	 * setup list for home timeline
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_HOME = 0xE7028B60;

	/**
	 * setup list for mention timeline
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_MENT = 0x9EC8274D;

	/**
	 * setup list for tweet timeline of a specific user
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_TWEETS = 0x4DBEF6CD;

	/**
	 * setup list for favorite timeline of a specific user
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_FAVORS = 0x8DE749EC;

	/**
	 * setup list for tweet replies of a specific tweet
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_ANSWER = 0xAFB5F1C0;

	/**
	 * setup list for search timeline
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_SEARCH = 0x91A71117;

	/**
	 * setup list for userlist timeline
	 *
	 * @see #KEY_FRAG_TWEET_MODE
	 */
	public static final int TWEET_FRAG_LIST = 0x43F518F7;

	/**
	 * replace all items from list
	 */
	public static final int CLEAR_LIST = -1;

	/**
	 * request code to check for tweet changes
	 */
	private static final int REQUEST_TWEET_CHANGED = 0xB90D;

	private TweetLoader tweetTask;
	private TweetAdapter adapter;

	private String search = "";
	private int mode = 0;
	private long id = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle param = getArguments();
		if (param != null) {
			mode = param.getInt(KEY_FRAG_TWEET_MODE, 0);
			id = param.getLong(KEY_FRAG_TWEET_ID, 0);
			search = param.getString(KEY_FRAG_TWEET_SEARCH, "");
		}
		adapter = new TweetAdapter(requireContext(), this);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (tweetTask == null) {
			load(0, 0, CLEAR_LIST);
			setRefresh(true);
		}
	}


	@Override
	protected void onReset() {
		adapter = new TweetAdapter(requireContext(), this);
		setAdapter(adapter);
		load(0, 0, CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		if (tweetTask != null && tweetTask.getStatus() == RUNNING) {
			tweetTask.cancel(true);
		}
		super.onDestroy();
	}


	@Override
	public void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
		super.onActivityResult(reqCode, returnCode, intent);
		if (intent != null && reqCode == REQUEST_TWEET_CHANGED) {
			if (returnCode == TweetActivity.RETURN_TWEET_UPDATE) {
				Object data = intent.getSerializableExtra(TweetActivity.INTENT_TWEET_UPDATE_DATA);
				if (data instanceof Tweet) {
					Tweet updateTweet = (Tweet) data;
					adapter.updateItem(updateTweet);
				}
			} else if (returnCode == TweetActivity.RETURN_TWEET_REMOVED) {
				long removedTweetId = intent.getLongExtra(TweetActivity.INTENT_TWEET_REMOVED_ID, 0);
				adapter.remove(removedTweetId);
			}
		}
	}


	@Override
	protected void onReload() {
		if (tweetTask != null && tweetTask.getStatus() != RUNNING) {
			long sinceId = 0;
			if (!adapter.isEmpty())
				sinceId = adapter.getItemId(0);
			load(sinceId, 0, 0);
		}
	}


	@Override
	public void onTweetClick(Tweet tweet) {
		if (!isRefreshing()) {
			Intent tweetIntent = new Intent(requireContext(), TweetActivity.class);
			tweetIntent.putExtra(KEY_TWEET_DATA, tweet);
			startActivityForResult(tweetIntent, REQUEST_TWEET_CHANGED);
		}
	}


	@Override
	public boolean onPlaceholderClick(long minId, long maxId, int pos) {
		if (tweetTask != null && tweetTask.getStatus() != RUNNING) {
			load(minId, maxId, pos);
			return true;
		}
		return false;
	}

	/**
	 * Set Tweet data to list
	 *
	 * @param tweets List of tweets
	 * @param pos    position where tweets should be added
	 */
	public void setData(List<Tweet> tweets, int pos) {
		if (pos == CLEAR_LIST) {
			adapter.replaceAll(tweets);
		} else {
			adapter.insertAt(tweets, pos);
		}
		setRefresh(false);
	}

	/**
	 * called from {@link TweetLoader} if an error occurs
	 *
	 * @param error Twitter exception
	 */
	public void onError(@Nullable ErrorHandler.TwitterError error) {
		ErrorHandler.handleFailure(requireContext(), error);
		adapter.disableLoading();
		setRefresh(false);
	}

	/**
	 * load content into the list
	 *
	 * @param sinceId ID where to start at
	 * @param maxId   ID where to stop
	 * @param index   index where tweet list should be added
	 */
	private void load(long sinceId, long maxId, int index) {
		switch (mode) {
			case TWEET_FRAG_HOME:
				tweetTask = new TweetLoader(this, TweetLoader.TL_HOME, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_MENT:
				tweetTask = new TweetLoader(this, TweetLoader.TL_MENT, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_TWEETS:
				tweetTask = new TweetLoader(this, TweetLoader.USR_TWEETS, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_FAVORS:
				tweetTask = new TweetLoader(this, TweetLoader.USR_FAVORS, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_ANSWER:
				if (tweetTask != null || settings.replyLoadingEnabled())
					tweetTask = new TweetLoader(this, TweetLoader.REPLIES, id, search, index);
				else
					tweetTask = new TweetLoader(this, TweetLoader.REPLIES_OFFLINE, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_SEARCH:
				tweetTask = new TweetLoader(this, TweetLoader.TWEET_SEARCH, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;

			case TWEET_FRAG_LIST:
				tweetTask = new TweetLoader(this, TweetLoader.USERLIST, id, search, index);
				tweetTask.execute(sinceId, maxId);
				break;
		}
	}
}