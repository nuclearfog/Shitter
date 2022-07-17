package org.nuclearfog.twidda.backend.async;

import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.ui.activities.TweetEditor.KEY_TWEETPOPUP_TEXT;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_DATA;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_DISABLE_RELOAD;
import static org.nuclearfog.twidda.ui.activities.UserlistActivity.KEY_LIST_ID;
import static org.nuclearfog.twidda.ui.activities.UserlistsActivity.KEY_USERLIST_OWNER_NAME;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.MainActivity;
import org.nuclearfog.twidda.ui.activities.MessageActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.activities.TweetActivity;
import org.nuclearfog.twidda.ui.activities.TweetEditor;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.activities.UserlistsActivity;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

/**
 * This class handles deep links and starts activities to show the content
 * When the user clicks on a link (e.g. https://twitter.com/Twitter/status/1480571976414543875)
 * this class extracts information of the link and open an activity tp show the content
 * When a link type isn't supported, the {@link MainActivity} will be opened instead
 *
 * @author nuclearfog
 * @see MainActivity
 */
public class LinkLoader extends AsyncTask<Uri, Void, LinkLoader.DataHolder> {

	private static final Pattern TWEET_PATH = Pattern.compile("[\\w]+/status/\\d+");
	private static final Pattern USER_PATH = Pattern.compile("[\\w]+/?(\\bwith_replies\\b|\\bmedia\\b|\\blikes\\b)?");
	private static final Pattern USERLISTS_PATH = Pattern.compile("[\\w]+/lists");
	private static final Pattern USERLIST_PATH = Pattern.compile("i/lists/\\d+");

	private WeakReference<MainActivity> weakRef;
	private TwitterException exception;
	private Twitter mTwitter;


	public LinkLoader(MainActivity activity) {
		super();
		weakRef = new WeakReference<>(activity);
		mTwitter = Twitter.get(activity);
	}


	@Override
	protected DataHolder doInBackground(Uri[] links) {
		DataHolder dataHolder = null;
		try {
			Uri link = links[0];
			String path = link.getPath();
			Bundle data = new Bundle();
			if (path != null && path.length() > 1) {
				path = path.substring(1);
				// open home timeline tab
				if (path.equals("home")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 0);
					dataHolder = new DataHolder(data, MainActivity.class);
				}
				// open trend tab
				else if (path.equals("i/trends") || path.equals("trends") || path.equals("explore")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 1);
					dataHolder = new DataHolder(data, MainActivity.class);
				}
				// open mentions timeline
				else if (path.equals("notifications")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 2);
					dataHolder = new DataHolder(data, MainActivity.class);
				}
				// open directmessage page
				else if (path.equals("messages")) {
					dataHolder = new DataHolder(data, MessageActivity.class);
				}
				// open twitter search
				else if (path.equals("search")) {
					if (link.isHierarchical()) {
						String search = link.getQueryParameter("q");
						if (search != null) {
							data.putString(KEY_SEARCH_QUERY, search);
							dataHolder = new DataHolder(data, SearchActivity.class);
						}
					}
				}
				// open tweet editor and add text
				else if (path.equals("intent/tweet") || path.equals("share")) {
					if (link.isHierarchical()) {
						String tweet = "";
						String text = link.getQueryParameter("text");
						String url = link.getQueryParameter("url");
						String via = link.getQueryParameter("via");
						if (text != null)
							tweet = text + " ";
						if (url != null)
							tweet += url + " ";
						if (via != null)
							tweet += "via @" + via;
						data.putString(KEY_TWEETPOPUP_TEXT, tweet);
						dataHolder = new DataHolder(data, TweetEditor.class);
					}
				}
				// open hashtag search
				else if (path.startsWith("hashtag/")) {
					String search = '#' + path.substring(8);
					data.putString(KEY_SEARCH_QUERY, search);
					dataHolder = new DataHolder(data, SearchActivity.class);
				}
				// open an userlist
				else if (USERLIST_PATH.matcher(path).matches()) {
					long listId = Long.parseLong(path.substring(8));
					data.putLong(KEY_LIST_ID, listId);
					dataHolder = new DataHolder(data, UserlistActivity.class);
				}
				// show user profile
				else if (USER_PATH.matcher(path).matches()) {
					int end = path.indexOf('/');
					if (end > 0)
						path = path.substring(0, end);
					User user = mTwitter.showUser(path);
					data.putSerializable(KEY_PROFILE_DATA, user);
					data.putBoolean(KEY_PROFILE_DISABLE_RELOAD, true);
					dataHolder = new DataHolder(data, ProfileActivity.class);
				} else {
					String username = '@' + path.substring(0, path.indexOf('/'));
					// show tweet
					if (TWEET_PATH.matcher(path).matches()) {
						long tweetId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
						data.putLong(KEY_TWEET_ID, tweetId);
						data.putString(KEY_TWEET_NAME, username);
						dataHolder = new DataHolder(data, TweetActivity.class);
					}
					// show userlists
					else if (USERLISTS_PATH.matcher(path).matches()) {
						data.putString(KEY_USERLIST_OWNER_NAME, username);
						dataHolder = new DataHolder(data, UserlistsActivity.class);
					}
				}
			}
		} catch (TwitterException e) {
			exception = e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataHolder;
	}


	@Override
	protected void onPostExecute(DataHolder result) {
		MainActivity activity = weakRef.get();
		if (activity != null) {
			if (exception != null) {
				activity.onError(exception);
			} else {
				activity.onSuccess(result);
			}
		}
	}


	/**
	 * Holder class for information to start an activity
	 */
	public static class DataHolder {
		@NonNull
		public final Bundle data;
		public final Class<? extends Activity> activity;

		DataHolder(@NonNull Bundle data, Class<? extends Activity> activity) {
			this.data = data;
			this.activity = activity;
		}
	}
}