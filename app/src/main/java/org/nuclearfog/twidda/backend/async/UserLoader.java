package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;

/**
 * Async class to load user information
 *
 * @author nuclearfog
 */
public class UserLoader extends AsyncExecutor<UserLoader.Param, UserLoader.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.LOCAL:
					User user = db.getUser(param.id);
					if (user != null) {
						return new Result(Result.LOCAL, user, null);
					}
					// fall through

				case Param.ONLINE:
					user = connection.showUser(param.id);
					db.saveUser(user);
					return new Result(Result.ONLINE, user, null);

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

		public static final int LOCAL = 1;
		public static final int ONLINE = 2;

		final int mode;
		final long id;

		public Param(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int LOCAL = 10;
		public static final int ONLINE = 11;

		@Nullable
		public final User user;
		@Nullable
		public final ConnectionException exception;
		public final int mode;

		Result(int mode, @Nullable User user, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.user = user;
			this.exception = exception;
		}
	}
}