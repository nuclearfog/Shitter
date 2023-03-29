package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.UserLists;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

/**
 * Background task for downloading twitter lists created by a user
 *
 * @author nuclearfog
 * @see UserListFragment
 */
public class ListLoader extends AsyncExecutor<ListLoader.UserlistParam, ListLoader.UserlistResult> {

	private Connection connection;

	/**
	 *
	 */
	public ListLoader(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected UserlistResult doInBackground(@NonNull UserlistParam param) {
		try {
			switch (param.mode) {
				case UserlistParam.OWNERSHIP:
					UserLists userlists = connection.getUserlistOwnerships(param.id, param.cursor);
					return new UserlistResult(UserlistResult.OWNERSHIP, param.index, userlists, null);

				case UserlistParam.MEMBERSHIP:
					userlists = connection.getUserlistMemberships(param.id, param.cursor);
					return new UserlistResult(UserlistResult.MEMBERSHIP, param.index, userlists, null);
			}
		} catch (ConnectionException exception) {
			return new UserlistResult(UserlistResult.ERROR, param.index, null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UserlistResult(UserlistResult.ERROR, param.index, null, null);
	}

	/**
	 *
	 */
	public static class UserlistParam {

		public static final int OWNERSHIP = 1;
		public static final int MEMBERSHIP = 2;

		final int mode, index;
		final long id, cursor;

		public UserlistParam(int mode, int index, long id, long cursor) {
			this.mode = mode;
			this.id = id;
			this.index = index;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class UserlistResult {

		public static final int ERROR = -1;
		public static final int OWNERSHIP = 3;
		public static final int MEMBERSHIP = 4;

		public final int mode, index;
		@Nullable
		public final UserLists userlists;
		@Nullable
		public final ConnectionException exception;

		UserlistResult(int mode, int index, @Nullable UserLists userlists, @Nullable ConnectionException exception) {
			this.userlists = userlists;
			this.exception = exception;
			this.mode = mode;
			this.index = index;
		}
	}
}