package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.FilterDatabase;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;

import java.lang.ref.WeakReference;

/**
 * This background task loads profile information about a twitter user and take actions
 *
 * @author nuclearfog
 * @see ProfileActivity
 */
public class UserAction extends AsyncTask<Void, User, Relation> {

	/**
	 * Load profile information
	 */
	public static final int PROFILE_lOAD = 1;

	/**
	 * load profile from database first
	 */
	public static final int PROFILE_DB = 2;

	/**
	 * follow user
	 */
	public static final int ACTION_FOLLOW = 3;

	/**
	 * un-follow user
	 */
	public static final int ACTION_UNFOLLOW = 4;

	/**
	 * block user
	 */
	public static final int ACTION_BLOCK = 5;

	/**
	 * un-block user
	 */
	public static final int ACTION_UNBLOCK = 6;

	/**
	 * mute user
	 */
	public static final int ACTION_MUTE = 7;

	/**
	 * un-mute user
	 */
	public static final int ACTION_UNMUTE = 8;


	private ConnectionException error;
	private WeakReference<ProfileActivity> weakRef;
	private Connection connection;
	private FilterDatabase filterDatabase;
	private AppDatabase appDB;
	private long userId;
	private int action;

	/**
	 * @param activity Callback to return the result
	 * @param userId   ID of the twitter user
	 */
	public UserAction(ProfileActivity activity, int action, long userId) {
		super();
		connection = Twitter.get(activity);
		appDB = new AppDatabase(activity);
		filterDatabase = new FilterDatabase(activity);
		this.weakRef = new WeakReference<>(activity);
		this.userId = userId;
		this.action = action;
	}


	@Override
	protected Relation doInBackground(Void... v) {
		try {
			switch (action) {
				case PROFILE_DB:
					// load user information from database
					User user;
					if (userId > 0) {
						user = appDB.getUser(userId);
						if (user != null) {
							publishProgress(user);
						}
					}

				case PROFILE_lOAD:
					// load user information from twitter
					user = connection.showUser(userId);
					publishProgress(user);
					appDB.storeUser(user);
					// load user relations from twitter
					Relation relation = connection.getRelationToUser(userId);
					if (!relation.isHome()) {
						boolean muteUser = relation.isBlocked() || relation.isMuted();
						appDB.muteUser(userId, muteUser);
					}
					return relation;

				case ACTION_FOLLOW:
					user = connection.followUser(userId);
					publishProgress(user);
					break;

				case ACTION_UNFOLLOW:
					user = connection.unfollowUser(userId);
					publishProgress(user);
					break;

				case ACTION_BLOCK:
					user = connection.blockUser(userId);
					publishProgress(user);
					appDB.muteUser(userId, true);
					break;

				case ACTION_UNBLOCK:
					user = connection.unblockUser(userId);
					publishProgress(user);
					// remove from exclude list only if user is not muted
					relation = connection.getRelationToUser(userId);
					if (!relation.isMuted()) {
						appDB.muteUser(userId, false);
						filterDatabase.removeUser(userId);
					}
					return relation;

				case ACTION_MUTE:
					user = connection.muteUser(userId);
					publishProgress(user);
					appDB.muteUser(userId, true);
					break;

				case ACTION_UNMUTE:
					user = connection.unmuteUser(userId);
					publishProgress(user);
					// remove from exclude list only if user is not blocked
					relation = connection.getRelationToUser(userId);
					if (!relation.isBlocked()) {
						appDB.muteUser(userId, false);
						filterDatabase.removeUser(userId);
					}
					return relation;
			}
			return connection.getRelationToUser(userId);
		} catch (ConnectionException exception) {
			this.error = exception;
		}
		return null;
	}


	@Override
	protected void onProgressUpdate(User[] users) {
		ProfileActivity activity = weakRef.get();
		if (activity != null) {
			activity.setUser(users[0]);
		}
	}


	@Override
	protected void onPostExecute(Relation relation) {
		ProfileActivity activity = weakRef.get();
		if (activity != null) {
			if (relation != null) {
				activity.onAction(relation);
			} else {
				activity.onError(error);
			}
		}
	}
}