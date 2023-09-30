package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.ui.activities.UsersActivity;

/**
 * Backend of {@link UsersActivity}
 * used to block/mute users/domains
 *
 * @author nuclearfog
 */
public class UserFilterAction extends AsyncExecutor<UserFilterAction.Param, UserFilterAction.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserFilterAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.MUTE_USER:
					Relation relation = connection.muteUser(param.name);
					db.muteUser(relation.getId(), true);
					return new Result(Result.MUTE_USER, null);

				case Param.BLOCK_USER:
					relation = connection.blockUser(param.name);
					db.muteUser(relation.getId(), true);
					return new Result(Result.BLOCK_USER, null);

				case Param.BLOCK_DOMAIN:
					connection.blockDomain(param.name);
					return new Result(Result.BLOCK_DOMAIN, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int MUTE_USER = 2;
		public static final int BLOCK_USER = 3;
		public static final int BLOCK_DOMAIN = 4;

		final String name;
		final int mode;

		public Param(int mode, String name) {
			this.mode = mode;
			this.name = name;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int MUTE_USER = 6;
		public static final int BLOCK_USER = 7;
		public static final int BLOCK_DOMAIN = 8;

		public final int mode;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.exception = exception;
		}
	}
}