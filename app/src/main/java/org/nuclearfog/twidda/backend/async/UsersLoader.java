package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.Users;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.ui.fragments.UserFragment;

/**
 * download a list of user such as follower, following or searched users
 *
 * @author nuclearfog
 * @see UserFragment
 */
public class UsersLoader extends AsyncExecutor<UsersLoader.UserParam, UsersLoader.UserResult> {

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
	 * load users reposting a status
	 */
	public static final int REPOST = 3;

	/**
	 * load users favoriting a status
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

	private Connection connection;

	/**
	 *
	 */
	public UsersLoader(Context context) {
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected UserResult doInBackground(UserParam param) {
		Users users = null;
		try {
			switch (param.type) {
				case FOLLOWS:
					users = connection.getFollower(param.id, param.cursor);
					break;

				case FRIENDS:
					users = connection.getFollowing(param.id, param.cursor);
					break;

				case REPOST:
					users = connection.getRepostingUsers(param.id, param.cursor);
					break;

				case FAVORIT:
					users = connection.getFavoritingUsers(param.id, param.cursor);
					break;

				case SEARCH:
					users = connection.searchUsers(param.search, param.cursor);
					break;

				case SUBSCRIBER:
					users = connection.getListSubscriber(param.id, param.cursor);
					break;

				case LISTMEMBER:
					users = connection.getListMember(param.id, param.cursor);
					break;

				case BLOCK:
					users = connection.getBlockedUsers(param.cursor);
					break;

				case MUTE:
					users = connection.getMutedUsers(param.cursor);
					break;

				case INCOMING_REQ:
					users =  connection.getIncomingFollowRequests(param.cursor);
					break;

				case OUTGOING_REQ:
					users =  connection.getOutgoingFollowRequests(param.cursor);
					break;
			}
		} catch (ConnectionException exception) {
			return new UserResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UserResult(users, null);
	}


	public static class UserParam {

		public final int type;
		public final String search;
		public final long id, cursor;

		public UserParam(int type, long id, long cursor, String search) {
			this.type = type;
			this.id = id;
			this.cursor = cursor;
			this.search = search;
		}
	}


	public static class UserResult {

		@Nullable
		public final Users users;
		@Nullable
		public final ConnectionException exception;

		public UserResult(@Nullable Users users, @Nullable ConnectionException exception) {
			this.users = users;
			this.exception = exception;
		}
	}
}