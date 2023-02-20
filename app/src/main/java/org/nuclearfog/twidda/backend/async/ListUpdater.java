package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.UserListUpdate;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.model.UserList;
import org.nuclearfog.twidda.ui.activities.UserlistEditor;

/**
 * This class creates and updates user lists
 * Backend for {@link UserlistEditor}
 *
 * @author nuclearfog
 */
public class ListUpdater extends AsyncExecutor<UserListUpdate, ListUpdater.ListUpdateResult> {

	private Connection connection;

	/**
	 *
	 */
	public ListUpdater(Context context) {
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected ListUpdateResult doInBackground(UserListUpdate update) {
		try {
			UserList result;
			if (update.exists())
				result = connection.updateUserlist(update);
			else
				result = connection.createUserlist(update);
			return new ListUpdateResult(result, update.exists(), null);
		} catch (ConnectionException exception) {
			return new ListUpdateResult(null, update.exists(), exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ListUpdateResult(null, update.exists(), null);
	}

	/**
	 *
	 */
	public static class ListUpdateResult {

		public final boolean updated;
		@Nullable
		public final UserList userlist;
		@Nullable
		public final ConnectionException exception;

		ListUpdateResult(@Nullable UserList userlist, boolean updated, @Nullable ConnectionException exception) {
			this.userlist = userlist;
			this.updated = updated;
			this.exception = exception;
		}
	}
}