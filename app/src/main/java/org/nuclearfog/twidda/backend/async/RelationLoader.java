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
 * This background task loads profile information about a twitter user and take actions
 *
 * @author nuclearfog
 * @see ProfileActivity
 */
public class RelationLoader extends AsyncExecutor<RelationLoader.RelationParam, RelationLoader.RelationResult> {

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
	protected RelationResult doInBackground(@NonNull RelationParam param) {
		try {
			switch (param.mode) {
				case RelationParam.LOAD:
					Relation relation = connection.getUserRelationship(param.id);
					return new RelationResult(RelationResult.LOAD, relation);

				case RelationParam.FOLLOW:
					relation = connection.followUser(param.id);
					return new RelationResult(RelationResult.FOLLOW, relation);

				case RelationParam.UNFOLLOW:
					relation = connection.unfollowUser(param.id);
					return new RelationResult(RelationResult.UNFOLLOW, relation);

				case RelationParam.BLOCK:
					relation = connection.blockUser(param.id);
					db.muteUser(param.id, true);
					db.saveUserToFilterlist(param.id);
					return new RelationResult(RelationResult.BLOCK, relation);

				case RelationParam.UNBLOCK:
					relation = connection.unblockUser(param.id);
					// remove from exclude list only if user is not muted
					if (!relation.isMuted()) {
						db.muteUser(param.id, false);
						db.removeUserFromFilterlist(param.id);
					}
					return new RelationResult(RelationResult.UNBLOCK, relation);

				case RelationParam.MUTE:
					relation = connection.muteUser(param.id);
					db.muteUser(param.id, true);
					return new RelationResult(RelationResult.MUTE, relation);

				case RelationParam.UNMUTE:
					relation = connection.unmuteUser(param.id);
					// remove from exclude list only if user is not blocked
					if (!relation.isBlocked()) {
						db.muteUser(param.id, false);
						db.removeUserFromFilterlist(param.id);
					}
					return new RelationResult(RelationResult.UNMUTE, relation);
			}
		} catch (ConnectionException exception) {
			return new RelationResult(RelationResult.ERROR, null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new RelationResult(RelationResult.ERROR, null, null);
	}

	/**
	 *
	 */
	public static class RelationParam {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int BLOCK = 4;
		public static final int UNBLOCK = 5;
		public static final int MUTE = 6;
		public static final int UNMUTE = 7;

		final long id;
		final int mode;

		public RelationParam(long id, int mode) {
			this.id = id;
			this.mode = mode;
		}
	}

	/**
	 *
	 */
	public static class RelationResult {

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

		RelationResult(int mode, Relation relation) {
			this(mode, relation, null);
		}

		RelationResult(int mode, @Nullable Relation relation, @Nullable ConnectionException exception) {
			this.relation = relation;
			this.exception = exception;
			this.mode = mode;
		}
	}
}