package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;

/**
 * async task to load list information and take action to the list
 *
 * @author nuclearfog
 */
public class UserlistAction extends AsyncExecutor<UserlistAction.Param, UserlistAction.Result> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			if (param.mode == Param.DELETE) {
				connection.deleteUserlist(param.id);
				return new Result(Result.DELETE, param.id, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.id, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int DELETE = 4;

		final int mode;
		final long id;

		/**
		 * @param action action to apply on the userlist {@link #DELETE}
		 * @param id     userlist ID
		 */
		public Param(int action, long id) {
			this.mode = action;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int DELETE = 8;
		public static final int ERROR = -1;

		public final int action;
		public final long id;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param action    action to apply on the userlist {@link #DELETE}
		 * @param id        userlist ID
		 * @param exception not null if an error occured
		 */
		Result(int action, long id, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.action = action;
			this.id = id;
		}
	}
}