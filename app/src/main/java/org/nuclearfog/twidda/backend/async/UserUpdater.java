package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.ProfileUpdate;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileEditor;

/**
 * Async loader to update user profile
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncExecutor<ProfileUpdate, UserUpdater.UserUpdateResult> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public UserUpdater(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@NonNull
	@Override
	protected UserUpdateResult doInBackground(@NonNull ProfileUpdate param) {
		try {
			User user = connection.updateProfile(param);
			db.saveUser(user);
			return new UserUpdateResult(user, null);
		} catch (ConnectionException exception) {
			return new UserUpdateResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			param.close();
		}
		return new UserUpdateResult(null, null);
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