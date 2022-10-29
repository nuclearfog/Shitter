package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.ui.fragments.UserFragment;

import java.lang.ref.WeakReference;

/**
 * download a list of user such as follower, following or searched users
 *
 * @author nuclearfog
 * @see UserFragment
 */
public class UserLoader extends AsyncTask<Long, Void, Users> {

	public static final long NO_CURSOR = -1;

	/**
	 * load follower list
	 */
	public static final int FOLLOWS = 1;

	/**
	 * load following list
	 */
	public static final int FRIENDS = 2;

	/**
	 * load users retweeting a tweet
	 */
	public static final int RETWEET = 3;

	/**
	 * load users favoriting a tweet
	 */
	public static final int FAVORIT = 4;

	/**
	 * list users of a search result
	 */
	public static final int SEARCH = 5;

	/**
	 * load users subscribing an userlist
	 */
	public static final int SUBSCRIBER = 6;

	/**
	 * load members of an userlist
	 */
	public static final int LISTMEMBER = 7;

	/**
	 * create a list of blocked users
	 */
	public static final int BLOCK = 8;

	/**
	 * create a list of muted users
	 */
	public static final int MUTE = 9;

	/**
	 * create a list with outgoing follow requests
	 */
	public static final int OUTGOING_REQ = 10;

	/**
	 * create a list with incoming follow requests
	 */
	public static final int INCOMING_REQ = 11;


	@Nullable
	private TwitterException twException;
	private final WeakReference<UserFragment> weakRef;
	private Twitter mTwitter;

	private final int type;
	private final String search;
	private final long id;

	/**
	 * @param fragment reference to {@link UserFragment}
	 * @param type     type of list to load
	 * @param id       ID depending on what list to load (user ID, tweet ID, list ID)
	 * @param search   search string if type is {@link #SEARCH} or empty
	 */
	public UserLoader(UserFragment fragment, int type, long id, String search) {
		super();
		mTwitter = Twitter.get(fragment.getContext());
		weakRef = new WeakReference<>(fragment);

		this.type = type;
		this.search = search;
		this.id = id;
	}


	@Override
	protected Users doInBackground(Long[] param) {
		try {
			long cursor = param[0];
			switch (type) {
				case FOLLOWS:
					return mTwitter.getFollower(id, cursor);

				case FRIENDS:
					return mTwitter.getFollowing(id, cursor);

				case RETWEET:
					return mTwitter.getRetweetingUsers(id);

				case FAVORIT:
					return mTwitter.getLikingUsers(id);

				case SEARCH:
					return mTwitter.searchUsers(search, cursor);

				case SUBSCRIBER:
					return mTwitter.getListSubscriber(id, cursor);

				case LISTMEMBER:
					return mTwitter.getListMember(id, cursor);

				case BLOCK:
					return mTwitter.getBlockedUsers(cursor);

				case MUTE:
					return mTwitter.getMutedUsers(cursor);

				case INCOMING_REQ:
					return mTwitter.getIncomingFollowRequests(cursor);

				case OUTGOING_REQ:
					return mTwitter.getOutgoingFollowRequests(cursor);
			}
		} catch (TwitterException twException) {
			this.twException = twException;
		}
		return null;
	}


	@Override
	protected void onPostExecute(Users users) {
		UserFragment fragment = weakRef.get();
		if (fragment != null) {
			if (users != null) {
				fragment.setData(users);
			} else {
				fragment.onError(twException);
			}
		}
	}
}