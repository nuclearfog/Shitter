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
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected UserlistResult doInBackground(UserlistParam param) {
		UserLists userlists = null;
		try {
			switch (param.type) {
				case UserlistParam.LOAD_USERLISTS:
					userlists = connection.getUserlistOwnerships(param.id, param.cursor);
					break;

				case UserlistParam.LOAD_MEMBERSHIPS:
					userlists = connection.getUserlistMemberships(param.id, param.cursor);
					break;
			}
		} catch (ConnectionException exception) {
			return new UserlistResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UserlistResult(userlists, null);
	}

	/**
	 *
	 */
	public static class UserlistParam {

		public static final int LOAD_USERLISTS = 1;
		public static final int LOAD_MEMBERSHIPS = 2;

		public final int type;
		public final long id, cursor;

		public UserlistParam(int type, long id, long cursor) {
			this.type = type;
			this.id = id;
			this.cursor = cursor;
		}
	}

	/**
	 *
	 */
	public static class UserlistResult {

		@Nullable
		public final UserLists userlists;
		@Nullable
		public final ConnectionException exception;

		UserlistResult(@Nullable UserLists userlists, @Nullable ConnectionException exception) {
			this.userlists = userlists;
			this.exception = exception;
		}
	}
}