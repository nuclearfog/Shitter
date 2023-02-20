package org.nuclearfog.twidda.backend.async;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.MainActivity;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.SearchActivity;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.activities.StatusEditor;
import org.nuclearfog.twidda.ui.activities.UserlistActivity;
import org.nuclearfog.twidda.ui.activities.UserlistsActivity;

import java.util.List;

/**
 * This class handles deep links and starts activities to show the content
 * When the user clicks on a link (e.g. "twitter.com/Twitter/status/1480571976414543875")
 * this class extracts information of the link and open an activity tp show the content
 * When a link type isn't supported, the {@link MainActivity} will be opened instead
 *
 * @author nuclearfog
 * @see MainActivity
 */
public class LinkLoader extends AsyncExecutor<Uri, LinkLoader.LinkResult> {

	private Connection connection;

	/**
	 *
	 */
	public LinkLoader(Context context) {
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected LinkResult doInBackground(Uri link) {
		try {
			List<String> pathSeg = link.getPathSegments();
			Bundle data = new Bundle();
			if (!pathSeg.isEmpty()) {
				// open home timeline tab
				// e.g. twitter.com/home
				if (pathSeg.get(0).equals("home")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 0);
					return new LinkResult(data, MainActivity.class);
				}
				// open trend tab
				// e.g. twitter.com/trends , twitter.com/explore or twitter.com/i/trends
				else if (pathSeg.get(0).equals("trends") || pathSeg.get(0).equals("explore") ||
						(pathSeg.size() == 2 && pathSeg.get(0).equals("i") && pathSeg.get(1).equals("trends"))) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 1);
					return new LinkResult(data, MainActivity.class);
				}
				// open mentions timeline
				// e.g. twitter.com/notifications
				else if (pathSeg.get(0).equals("notifications")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 2);
					return new LinkResult(data, MainActivity.class);
				}
				// open directmessage page
				// e.g. twitter.com/messages
				else if (pathSeg.get(0).equals("messages")) {
					data.putInt(MainActivity.KEY_TAB_PAGE, 3);
					return new LinkResult(data, MainActivity.class);
				}
				// open twitter search
				// e.g. twitter.com/search?q={search string}
				else if (pathSeg.get(0).equals("search")) {
					if (link.isHierarchical()) {
						String search = link.getQueryParameter("q");
						if (search != null) {
							data.putString(SearchActivity.KEY_SEARCH_QUERY, search);
							return new LinkResult(data, SearchActivity.class);
						}
					}
				}
				// open status editor and add text
				// e.g. twitter.com/share or twitter.com/intent/status
				else if (pathSeg.get(0).equals("share") ||
						(pathSeg.size() == 2 && pathSeg.get(0).equals("intent") && pathSeg.get(1).equals("tweet"))) {
					if (link.isHierarchical()) {
						String status = "";
						String text = link.getQueryParameter("text");
						String url = link.getQueryParameter("url");
						String via = link.getQueryParameter("via");
						if (text != null)
							status = text + " ";
						if (url != null)
							status += url + " ";
						if (via != null)
							status += "via @" + via;
						data.putString(StatusEditor.KEY_STATUS_EDITOR_TEXT, status);
						return new LinkResult(data, StatusEditor.class);
					}
				}
				// open hashtag search
				// e.g. twitter.com/hashtag/{hashtag name}
				else if (pathSeg.size() == 2 && pathSeg.get(0).equals("hashtag")) {
					String search = '#' + pathSeg.get(1);
					data.putString(SearchActivity.KEY_SEARCH_QUERY, search);
					return new LinkResult(data, SearchActivity.class);
				}
				// open an userlist
				// e.g. twitter.com/i/lists/{list id}
				else if (pathSeg.size() == 3 && pathSeg.get(0).equals("i") && pathSeg.get(1).equals("lists") && pathSeg.get(2).matches("\\d+")) {
					long listId = Long.parseLong(pathSeg.get(2));
					UserList list = connection.getUserlist(listId);
					data.putSerializable(UserlistActivity.KEY_LIST_DATA, list);
					data.putBoolean(UserlistActivity.KEY_LIST_NO_UPDATE, true);
					return new LinkResult(data, UserlistActivity.class);
				}
				// show status
				// e.g. twitter.com/{screenname}/status/{tweet ID}
				else if (pathSeg.size() == 3 && pathSeg.get(1).equals("status") && pathSeg.get(2).matches("\\d+")) {
					String screenname = pathSeg.get(0);
					long Id = Long.parseLong(pathSeg.get(2));
					data.putLong(StatusActivity.KEY_STATUS_ID, Id);
					data.putString(StatusActivity.KEY_STATUS_NAME, screenname);
					return new LinkResult(data, StatusActivity.class);
				}
				// show userlists
				// e.g. twitter.com/{screenname}/lists
				else if (pathSeg.size() == 2 && pathSeg.get(1).equals("lists")) {
					String screenname = pathSeg.get(0);
					User user = connection.showUser(screenname);
					data.putLong(UserlistsActivity.KEY_USERLIST_OWNER_ID, user.getId());
					return new LinkResult(data, UserlistsActivity.class);
				}
				// show user profile
				// e.g. twitter.com/{screenname}
				else if (pathSeg.size() == 1 || (pathSeg.size() == 2 &&
						(pathSeg.get(1).equals("with_replies") || pathSeg.get(1).equals("media") || pathSeg.get(1).equals("likes")))) {
					String screenname = pathSeg.get(0);
					User user = connection.showUser(screenname);
					data.putSerializable(ProfileActivity.KEY_PROFILE_USER, user);
					return new LinkResult(data, ProfileActivity.class);
				}
			}
		} catch (ConnectionException exception) {
			return new LinkResult(null, null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new LinkResult(null, null, null);
	}

	/**
	 * Holder class for information to start an activity
	 */
	public static class LinkResult {

		@Nullable
		public final Bundle data;
		@Nullable
		public final Class<? extends Activity> activity;
		@Nullable
		public final ConnectionException exception;

		LinkResult(@NonNull Bundle data, @Nullable Class<? extends Activity> activity) {
			this(data, activity, null);
		}

		LinkResult(@Nullable Bundle data, @Nullable Class<? extends Activity> activity, @Nullable ConnectionException exception) {
			this.data = data;
			this.activity = activity;
			this.exception = exception;
		}
	}
}