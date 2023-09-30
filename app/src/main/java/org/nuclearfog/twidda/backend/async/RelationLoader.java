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
 * This background task loads profile information about an user and take actions
 *
 * @author nuclearfog
 * @see ProfileActivity
 */
public class RelationLoader extends AsyncExecutor<RelationLoader.Param, RelationLoader.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public RelationLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.LOAD:
					Relation relation = connection.getUserRelationship(param.id);
					return new Result(Result.LOAD, relation);

				case Param.FOLLOW:
					relation = connection.followUser(param.id);
					return new Result(Result.FOLLOW, relation);

				case Param.UNFOLLOW:
					relation = connection.unfollowUser(param.id);
					return new Result(Result.UNFOLLOW, relation);

				case Param.BLOCK:
					relation = connection.blockUser(param.id);
					db.muteUser(param.id, true);
					return new Result(Result.BLOCK, relation);

				case Param.UNBLOCK:
					relation = connection.unblockUser(param.id);
					// remove from exclude list only if user is not muted
					if (!relation.isMuted()) {
						db.muteUser(param.id, false);
					}
					return new Result(Result.UNBLOCK, relation);

				case Param.MUTE:
					relation = connection.muteUser(param.id);
					db.muteUser(param.id, true);
					return new Result(Result.MUTE, relation);

				case Param.UNMUTE:
					relation = connection.unmuteUser(param.id);
					// remove from exclude list only if user is not blocked
					if (!relation.isBlocked()) {
						db.muteUser(param.id, false);
					}
					return new Result(Result.UNMUTE, relation);

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
		final int mode;

		public Param(long id, int mode) {
			this.id = id;
			this.mode = mode;
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

		public final int mode;
		@Nullable
		public final Relation relation;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, Relation relation) {
			this(mode, relation, null);
		}

		Result(int mode, @Nullable Relation relation, @Nullable ConnectionException exception) {
			this.relation = relation;
			this.exception = exception;
			this.mode = mode;
		}
	}
}