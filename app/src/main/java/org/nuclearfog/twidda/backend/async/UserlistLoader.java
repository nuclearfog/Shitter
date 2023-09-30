package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.UserLists;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * Background task for downloading  userlists created by a user
 *
 * @author nuclearfog
 * @see UserListFragment
 */
public class UserlistLoader extends AsyncExecutor<UserlistLoader.Param, UserlistLoader.Result> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			switch (param.mode) {
				case Param.OWNERSHIP:
					UserLists userlists = connection.getUserlistOwnerships(param.id, param.cursor);
					return new Result(Result.OWNERSHIP, param.index, userlists, null);

				case Param.MEMBERSHIP:
					userlists = connection.getUserlistMemberships(param.id, param.cursor);
					return new Result(Result.MEMBERSHIP, param.index, userlists, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, param.index, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final long NO_CURSOR = -1L;

		public static final int OWNERSHIP = 1;
		public static final int MEMBERSHIP = 2;

		final int mode, index;
		final long id, cursor;

		public Param(int mode, int index, long id, long cursor) {
			this.mode = mode;
			this.id = id;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int OWNERSHIP = 3;
		public static final int MEMBERSHIP = 4;

		public final int mode, index;
		@Nullable
		public final UserLists userlists;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, int index, @Nullable UserLists userlists, @Nullable ConnectionException exception) {
			this.userlists = userlists;
			this.exception = exception;
			this.mode = mode;
			this.index = index;
		}
	}
}