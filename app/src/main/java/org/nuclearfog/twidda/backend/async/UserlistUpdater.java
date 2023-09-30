package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.UserListUpdate;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.UserlistEditor;

/**
 * This class creates and updates user lists
 * Backend for {@link UserlistEditor}
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
			if (update.getId() != 0L) {
				UserList result = connection.updateUserlist(update);
				return new Result(result, true, null);
			} else {
				UserList result = connection.createUserlist(update);
				return new Result(result, false, null);
			}
		} catch (ConnectionException exception) {
			return new Result(null, false, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final boolean updated;
		@Nullable
		public final UserList userlist;
		@Nullable
		public final ConnectionException exception;

		Result(@Nullable UserList userlist, boolean updated, @Nullable ConnectionException exception) {
			this.userlist = userlist;
			this.updated = updated;
			this.exception = exception;
		}
	}
}