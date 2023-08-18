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
public class UserlistManager extends AsyncExecutor<UserlistManager.ListManagerParam, UserlistManager.ListManagerResult> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistManager(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected ListManagerResult doInBackground(@NonNull ListManagerParam param) {
		try {
			switch (param.mode) {
				case ListManagerParam.ADD:
					connection.addUserToList(param.id, param.username);
					return new ListManagerResult(ListManagerResult.ADD_USER, param.username, null);

				case ListManagerParam.REMOVE:
					connection.removeUserFromList(param.id, param.username);
					return new ListManagerResult(ListManagerResult.DEL_USER, param.username, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new ListManagerResult(ListManagerResult.ERROR, param.username, exception);
		}
	}

	/**
	 *
	 */
	public static class ListManagerParam {

		public static final int ADD = 1;
		public static final int REMOVE = 2;

		final int mode;
		final long id;
		final String username;

		public ListManagerParam(int mode, long id, String username) {
			this.id = id;
			this.mode = mode;
			this.username = username;
		}
	}

	/**
	 *
	 */
	public static class ListManagerResult {

		public static final int ERROR = -1;
		public static final int ADD_USER = 3;
		public static final int DEL_USER = 4;

		public final int mode;
		public final String name;
		@Nullable
		public final ConnectionException exception;

		ListManagerResult(int mode, String name, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.name = name;
			this.exception = exception;
		}
	}
}