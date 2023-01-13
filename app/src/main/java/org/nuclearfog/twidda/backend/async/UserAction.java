package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
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
	private AppDatabase db;
	private long userId;
	private int action;

	/**
	 * @param activity Callback to return the result
	 * @param userId   ID of the twitter user
	 */
	public UserAction(ProfileActivity activity, int action, long userId) {
		super();
		connection = ConnectionManager.get(activity);
		db = new AppDatabase(activity);
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
						user = db.getUser(userId);
						publishProgress(user);
					}

				case PROFILE_lOAD:
					// load user information from twitter
					user = connection.showUser(userId);
					publishProgress(user);
					db.saveUser(user);
					// load user relations from twitter
					Relation relation = connection.getUserRelationship(userId);
					if (!relation.isCurrentUser()) {
						boolean muteUser = relation.isBlocked() || relation.isMuted();
						db.muteUser(userId, muteUser);
					}
					return relation;

				case ACTION_FOLLOW:
					connection.followUser(userId);
					break;

				case ACTION_UNFOLLOW:
					connection.unfollowUser(userId);
					break;

				case ACTION_BLOCK:
					connection.blockUser(userId);
					db.muteUser(userId, true);
					db.addUserToFilterlist(userId);
					break;

				case ACTION_UNBLOCK:
					connection.unblockUser(userId);
					// remove from exclude list only if user is not muted
					relation = connection.getUserRelationship(userId);
					if (!relation.isMuted()) {
						db.muteUser(userId, false);
						db.removeUserFromFilterlist(userId);
					}
					return relation;

				case ACTION_MUTE:
					connection.muteUser(userId);
					db.muteUser(userId, true);
					break;

				case ACTION_UNMUTE:
					connection.unmuteUser(userId);
					// remove from exclude list only if user is not blocked
					relation = connection.getUserRelationship(userId);
					if (!relation.isBlocked()) {
						db.muteUser(userId, false);
						db.removeUserFromFilterlist(userId);
					}
					return relation;
			}
			return connection.getUserRelationship(userId);
		} catch (ConnectionException exception) {
			this.error = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onProgressUpdate(User[] users) {
		ProfileActivity activity = weakRef.get();
		if (activity != null && users[0] != null) {
			activity.setUser(users[0]);
		}
	}


	@Override
	protected void onPostExecute(@Nullable Relation relation) {
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