package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileEditor;

/**
 * Async loader to update user profile
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncExecutor<UserUpdate, UserUpdater.UserUpdateResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserUpdater(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected UserUpdateResult doInBackground(@NonNull UserUpdate param) {
		try {
			User user = connection.updateUser(param);
			db.saveUser(user);
			return new UserUpdateResult(user, null);
		} catch (ConnectionException exception) {
			return new UserUpdateResult(null, exception);
		} finally {
			param.close();
		}
	}

	/**
	 *
	 */
	public static class UserUpdateResult {

		@Nullable
		public final User user;
		@Nullable
		public final ConnectionException exception;

		UserUpdateResult(@Nullable User user, @Nullable ConnectionException exception) {
			this.user = user;
			this.exception = exception;
		}
	}
}