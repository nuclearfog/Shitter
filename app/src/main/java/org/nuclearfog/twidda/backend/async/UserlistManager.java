package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * Backend async task to manage users on lists
 * Users can be added and removed
 *
 * @author nuclearfog
 */
public class UserlistManager extends AsyncExecutor<UserlistManager.Param, UserlistManager.Result> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistManager(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.ADD:
					connection.addUserToList(param.id, param.username);
					return new Result(Result.ADD_USER, param.username, null);

				case Param.REMOVE:
					connection.removeUserFromList(param.id, param.username);
					return new Result(Result.DEL_USER, param.username, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.username, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int ADD = 1;
		public static final int REMOVE = 2;

		final int mode;
		final long id;
		final String username;

		public Param(int mode, long id, String username) {
			this.id = id;
			this.mode = mode;
			this.username = username;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int ADD_USER = 3;
		public static final int DEL_USER = 4;

		public final int mode;
		public final String name;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, String name, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.name = name;
			this.exception = exception;
		}
	}
}