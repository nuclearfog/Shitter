package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;

/**
 * Ascync loader to load/update user relationships
 *
 * @author nuclearfog
 * @see ProfileActivity
 */
public class UserAction extends AsyncExecutor<UserAction.Param, UserAction.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.action) {
				case Param.LOAD:
					Relation relation = connection.getUserRelationship(param.id);
					return new Result(Result.LOAD, relation, null);

				case Param.FOLLOW:
					relation = connection.followUser(param.id);
					return new Result(Result.FOLLOW, relation, null);

				case Param.UNFOLLOW:
					relation = connection.unfollowUser(param.id);
					return new Result(Result.UNFOLLOW, relation, null);

				case Param.BLOCK:
					relation = connection.blockUser(param.id);
					db.muteUser(param.id, true);
					return new Result(Result.BLOCK, relation, null);

				case Param.UNBLOCK:
					relation = connection.unblockUser(param.id);
					// remove from exclude list only if user is not muted
					if (!relation.isMuted()) {
						db.muteUser(param.id, false);
					}
					return new Result(Result.UNBLOCK, relation, null);

				case Param.MUTE:
					relation = connection.muteUser(param.id);
					db.muteUser(param.id, true);
					return new Result(Result.MUTE, relation, null);

				case Param.UNMUTE:
					relation = connection.unmuteUser(param.id);
					// remove from exclude list only if user is not blocked
					if (!relation.isBlocked()) {
						db.muteUser(param.id, false);
					}
					return new Result(Result.UNMUTE, relation, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int BLOCK = 4;
		public static final int UNBLOCK = 5;
		public static final int MUTE = 6;
		public static final int UNMUTE = 7;

		final long id;
		final int action;

		/**
		 * @param id     user ID
		 * @param action action to perform on the user {@link #FOLLOW,#UNFOLLOW,#BLOCK,#UNBLOCK,#MUTE,#UNMUTE}
		 */
		public Param(long id, int action) {
			this.id = id;
			this.action = action;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int LOAD = 8;
		public static final int FOLLOW = 9;
		public static final int UNFOLLOW = 10;
		public static final int BLOCK = 11;
		public static final int UNBLOCK = 12;
		public static final int MUTE = 13;
		public static final int UNMUTE = 14;
		public static final int ERROR = -1;

		public final int action;
		@Nullable
		public final Relation relation;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param action   action performed on user
		 * @param relation updated relationship
		 */
		Result(int action, @Nullable Relation relation, @Nullable ConnectionException exception) {
			this.relation = relation;
			this.exception = exception;
			this.action = action;
		}
	}
}