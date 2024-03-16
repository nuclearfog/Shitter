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

/**
 * Async updater class used to update current user information and profile settings
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.activities.ProfileEditor
 */
public class CredentialsUpdater extends AsyncExecutor<UserUpdate, CredentialsUpdater.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public CredentialsUpdater(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull UserUpdate param) {
		try {
			User user = connection.updateCredentials(param);
			db.saveUser(user);
			param.close();
			return new Result(user, null);
		} catch (ConnectionException exception) {
			return new Result(null, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final User user;
		@Nullable
		public final ConnectionException exception;

		/**
		 * @param user updated information from the current user or null if an error occured
		 */
		Result(@Nullable User user, @Nullable ConnectionException exception) {
			this.user = user;
			this.exception = exception;
		}
	}
}