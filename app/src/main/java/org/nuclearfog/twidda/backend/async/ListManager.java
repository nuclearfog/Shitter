package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;

/**
 * Backend async task to manage users on lists
 * Twitter users can be added and removed
 *
 * @author nuclearfog
 */
public class ListManager extends AsyncExecutor<ListManager.ListManagerParam, ListManager.ListManagerResult> {

	private Connection connection;

	/**
	 *
	 */
	public ListManager(Context context) {
		connection = ConnectionManager.get(context);

	}


	@NonNull
	@Override
	protected ListManagerResult doInBackground(ListManagerParam param) {
		try {
			switch (param.mode) {
				case ListManagerParam.ADD_USER:
					connection.addUserToList(param.id, param.username);
					return new ListManagerResult(ListManagerResult.ADD_USER, param.username, null);

				case ListManagerParam.DEL_USER:
					connection.removeUserFromList(param.id, param.username);
					return new ListManagerResult(ListManagerResult.DEL_USER, param.username, null);
			}
		} catch (ConnectionException exception) {
			return new ListManagerResult(ListManagerResult.ERROR, param.username, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ListManagerResult(ListManagerResult.ERROR, param.username, null);
	}

	/**
	 *
	 */
	public static class ListManagerParam {

		public static final int ADD_USER = 1;
		public static final int DEL_USER = 2;

		public final long id;
		public final String username;
		public final int mode;

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