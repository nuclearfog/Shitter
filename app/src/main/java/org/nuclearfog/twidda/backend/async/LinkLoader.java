package org.nuclearfog.twidda.backend.async;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.MainActivity;
import org.nuclearfog.twidda.ui.activities.MessageActivity;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.activities.TweetActivity;
import org.nuclearfog.twidda.ui.activities.TweetEditor;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.activities.UserlistsActivity;

import java.lang.ref.WeakReference;
import java.util.List;

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

	private WeakReference<MainActivity> weakRef;
	private Connection connection;

	@Nullable
	private ConnectionException exception;


	public LinkLoader(MainActivity activity) {
		super();
		weakRef = new WeakReference<>(activity);
		connection = Twitter.get(activity);
	}


	@Override
	protected DataHolder doInBackground(Uri[] links) {
		try {
			Uri link = links[0];
			List<String> pathSeg = link.getPathSegments();
			Bundle data = new Bundle();
			if (!pathSeg.isEmpty()) {
				// open home timeline tab
				// e.g. twitter.com/home
				if (pathSeg.get(0).equals("home")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 0);
					return new DataHolder(data, MainActivity.class);
				}
				// open trend tab
				// e.g. twitter.com/trends , twitter.com/explore or twitter.com/i/trends
				else if (pathSeg.get(0).equals("trends") || pathSeg.get(0).equals("explore") ||
						(pathSeg.size() == 2 && pathSeg.get(0).equals("i") && pathSeg.get(1).equals("trends"))) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 1);
					return new DataHolder(data, MainActivity.class);
				}
				// open mentions timeline
				// e.g. twitter.com/notifications
				else if (pathSeg.get(0).equals("notifications")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 2);
					return new DataHolder(data, MainActivity.class);
				}
				// open directmessage page
				// e.g. twitter.com/messages
				else if (pathSeg.get(0).equals("messages")) {
					return new DataHolder(data, MessageActivity.class);
				}
				// open twitter search
				// e.g. twitter.com/search?q={search string}
				else if (pathSeg.get(0).equals("search")) {
					if (link.isHierarchical()) {
						String search = link.getQueryParameter("q");
						if (search != null) {
							data.putString(SearchActivity.KEY_SEARCH_QUERY, search);
							return new DataHolder(data, SearchActivity.class);
						}
					}
				}
				// open tweet editor and add text
				// e.g. twitter.com/share or twitter.com/intent/tweet
				else if (pathSeg.get(0).equals("share") ||
						(pathSeg.size() == 2 && pathSeg.get(0).equals("intent") && pathSeg.get(1).equals("tweet"))) {
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
						data.putString(TweetEditor.KEY_TWEETPOPUP_TEXT, tweet);
						return new DataHolder(data, TweetEditor.class);
					}
				}
				// open hashtag search
				// e.g. twitter.com/hashtag/{hashtag name}
				else if (pathSeg.size() == 2 && pathSeg.get(0).equals("hashtag")) {
					String search = '#' + pathSeg.get(1);
					data.putString(SearchActivity.KEY_SEARCH_QUERY, search);
					return new DataHolder(data, SearchActivity.class);
				}
				// open an userlist
				// e.g. twitter.com/i/lists/{list id}
				else if (pathSeg.size() == 3 && pathSeg.get(0).equals("i") && pathSeg.get(1).equals("lists") && pathSeg.get(2).matches("\\d+")) {
					long listId = Long.parseLong(pathSeg.get(2));
					UserList list = connection.getUserlist(listId);
					data.putSerializable(UserlistActivity.KEY_LIST_DATA, list);
					data.putBoolean(UserlistActivity.KEY_LIST_NO_UPDATE, true);
					return new DataHolder(data, UserlistActivity.class);
				}
				// show tweet
				// e.g. twitter.com/{screenname}/status/{tweet ID}
				else if (pathSeg.size() == 3 && pathSeg.get(1).equals("status") && pathSeg.get(2).matches("\\d+")) {
					String screenname = pathSeg.get(0);
					long tweetId = Long.parseLong(pathSeg.get(2));
					data.putLong(TweetActivity.KEY_TWEET_ID, tweetId);
					data.putString(TweetActivity.KEY_TWEET_NAME, screenname);
					return new DataHolder(data, TweetActivity.class);
				}
				// show userlists
				// e.g. twitter.com/{screenname}/lists
				else if (pathSeg.size() == 2 && pathSeg.get(1).equals("lists")) {
					String screenname = pathSeg.get(0);
					data.putString(UserlistsActivity.KEY_USERLIST_OWNER_NAME, screenname);
					return new DataHolder(data, UserlistsActivity.class);
				}
				// show user profile
				// e.g. twitter.com/{screenname}
				else if (pathSeg.size() == 1 || (pathSeg.size() == 2 &&
						(pathSeg.get(1).equals("with_replies") || pathSeg.get(1).equals("media") || pathSeg.get(1).equals("likes")))) {
					String screenname = pathSeg.get(0);
					User user = connection.showUser(screenname);
					data.putSerializable(ProfileActivity.KEY_PROFILE_DATA, user);
					data.putBoolean(ProfileActivity.KEY_PROFILE_DISABLE_RELOAD, true);
					return new DataHolder(data, ProfileActivity.class);
				}
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		}
		return null;
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