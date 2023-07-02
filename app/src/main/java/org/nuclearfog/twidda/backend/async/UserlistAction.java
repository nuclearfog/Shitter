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
public class UserlistAction extends AsyncExecutor<UserlistAction.ListActionParam, UserlistAction.ListActionResult> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistAction(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected ListActionResult doInBackground(@NonNull ListActionParam param) {
		try {
			switch (param.mode) {
				case ListActionParam.LOAD:
					UserList result = connection.getUserlist(param.id);
					return new ListActionResult(ListActionResult.LOAD, param.id, result, null);

				case ListActionParam.FOLLOW:
					result = connection.followUserlist(param.id);
					return new ListActionResult(ListActionResult.FOLLOW, param.id, result, null);

				case ListActionParam.UNFOLLOW:
					result = connection.unfollowUserlist(param.id);
					return new ListActionResult(ListActionResult.UNFOLLOW, param.id, result, null);

				case ListActionParam.DELETE:
					connection.deleteUserlist(param.id);
					return new ListActionResult(ListActionResult.DELETE, param.id, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new ListActionResult(ListActionResult.ERROR, param.id, null, exception);
		}
	}

	/**
	 *
	 */
	public static class ListActionParam {

		public static final int LOAD = 1;
		public static final int FOLLOW = 2;
		public static final int UNFOLLOW = 3;
		public static final int DELETE = 4;

		final int mode;
		final long id;

		public ListActionParam(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class ListActionResult {

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

		ListActionResult(int mode, long id, @Nullable UserList userlist, @Nullable ConnectionException exception) {
			this.userlist = userlist;
			this.exception = exception;
			this.mode = mode;
			this.id = id;
		}
	}
}