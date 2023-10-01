package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.model.UserList;

/**
 * This class creates and updates user lists
 * Backend for {@link org.nuclearfog.twidda.ui.dialogs.UserlistDialog}
 *
 * @author nuclearfog
 */
public class UserlistUpdater extends AsyncExecutor<UserListUpdate, UserlistUpdater.Result> {

	private Connection connection;

	/**
	 *
	 */
	public UserlistUpdater(Context context) {
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull UserListUpdate update) {
		try {
			UserList result = connection.updateUserlist(update);
			if (update.getId() != 0L)
				return new Result(Result.UPDATED, result, null);
			return new Result(Result.CREATED, result, null);
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int CREATED = 1;
		public static final int UPDATED = 2;
		public static final int ERROR = -1;

		public final int mode;
		@Nullable
		public final UserList userlist;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, @Nullable UserList userlist, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.userlist = userlist;
			this.exception = exception;
		}
	}
}