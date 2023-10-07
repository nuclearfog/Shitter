package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.update.UserUpdate;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.User;

/**
 * loader class for user credentials
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.activities.ProfileEditor
 */
public class CredentialsAction extends AsyncExecutor<CredentialsAction.Param, CredentialsAction.Result> {

	private Connection connection;
	private AppDatabase db;

	/**
	 *
	 */
	public CredentialsAction(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		try {
			if (param.mode == Param.UPDATE) {
				if (param.update != null) {
					User user = connection.updateCredentials(param.update);
					db.saveUser(user);
					param.update.close();
					return new Result(Result.UPDATE, user, null, null);
				}
			} else if (param.mode == Param.LOAD) {
				Credentials credentials = connection.getCredentials();
				return new Result(Result.LOAD, null, credentials, null);
			}
		} catch (ConnectionException exception) {
			return new Result(Result.ERROR, null, null, exception);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int LOAD = 1;
		public static final int UPDATE = 2;

		final int mode;
		@Nullable
		final UserUpdate update;

		public Param(int mode, @Nullable UserUpdate update) {
			this.mode = mode;
			this.update = update;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int ERROR = -1;
		public static final int LOAD = 10;
		public static final int UPDATE = 20;

		public final int mode;
		@Nullable
		public final User user;
		@Nullable
		public final Credentials credentials;
		@Nullable
		public final ConnectionException exception;

		Result(int mode, @Nullable User user, @Nullable Credentials credentials, @Nullable ConnectionException exception) {
			this.mode = mode;
			this.user = user;
			this.credentials = credentials;
			this.exception = exception;
		}
	}
}