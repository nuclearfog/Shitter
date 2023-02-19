package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.ProfileUpdate;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileEditor;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncExecutor<ProfileUpdate, UserUpdater.UserUpdateResult> {

	private Connection connection;
	private AppDatabase db;


	public UserUpdater(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.get(context);
	}


	@NonNull
	@Override
	protected UserUpdateResult doInBackground(ProfileUpdate param) {
		User user = null;
		try {
			user = connection.updateProfile(param);
			// save new user information
			db.saveUser(user);
		} catch (ConnectionException exception) {
			return new UserUpdateResult(null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// close image streams
			param.close();
		}
		return new UserUpdateResult(user, null);
	}


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