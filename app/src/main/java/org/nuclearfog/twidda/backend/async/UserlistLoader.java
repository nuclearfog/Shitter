package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.model.lists.UserLists;

/**
 * Background task for downloading  userlists created by a user
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.fragments.UserListFragment
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
			switch (param.type) {
				case Param.OWNERSHIP:
					UserLists userlists = connection.getUserlistOwnerships(param.id, param.cursor);
					return new Result(param.index, userlists, null);

				case Param.MEMBERSHIP:
					userlists = connection.getUserlistMemberships(param.id, param.cursor);
					return new Result(param.index, userlists, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(param.index, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final long NO_CURSOR = -1L;

		public static final int OWNERSHIP = 1;
		public static final int MEMBERSHIP = 2;

		final int type, index;
		final long id, cursor;

		/**
		 * @param type   type of userlsits to load
		 * @param index  index where to insert new items in the list/adapter
		 * @param id     userlist ID
		 * @param cursor cursor to parse the results
		 */
		public Param(int type, int index, long id, long cursor) {
			this.type = type;
			this.id = id;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final int index;
		@Nullable
		public final UserLists userlists;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param index     index where to insert new items in the list/adapter
		 * @param userlists result or null if an error occured
		 * @param exception if not null an error occured
		 */
		Result(int index, @Nullable UserLists userlists, @Nullable ConnectionException exception) {
			this.userlists = userlists;
			this.exception = exception;
			this.index = index;
		}
	}
}