package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.UserList;

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
			switch (param.mode) {
				case Param.LOAD:
					UserList result = connection.getUserlist(param.id);
					return new Result(Result.LOAD, param.id, result, null);

				case Param.FOLLOW:
					result = connection.followUserlist(param.id);
					return new Result(Result.FOLLOW, param.id, result, null);

				case Param.UNFOLLOW:
					result = connection.unfollowUserlist(param.id);
					return new Result(Result.UNFOLLOW, param.id, result, null);

				case Param.DELETE:
					connection.deleteUserlist(param.id);
					return new Result(Result.DELETE, param.id, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.id, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int DELETE = 4;

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

		public static final int LOAD = 5;
		public static final int FOLLOW = 6;
		public static final int UNFOLLOW = 7;
		public static final int DELETE = 8;
		public static final int ERROR = -1;

		public final int mode;
		public final long id;
		@Nullable
		public final UserList userlist;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, long id, @Nullable UserList userlist, @Nullable ConnectionException exception) {
			this.userlist = userlist;
			this.exception = exception;
			this.mode = mode;
			this.id = id;
		}
	}
}