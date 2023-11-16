package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.User;

/**
 * Asyncloader used to load information about the current user
 *
 * @author nuclearfog
 */
public class CredentialsLoader extends AsyncExecutor<Void, CredentialsLoader.Result> {

	private Connection connection;
	private GlobalSettings settings;
	private AppDatabase db;


	public CredentialsLoader(Context context) {
		db = new AppDatabase(context);
		connection = ConnectionManager.getDefaultConnection(context);
		settings = GlobalSettings.get(context);
	}


	@Override
	protected Result doInBackground(@NonNull Void param) {
		try {
			Credentials credentials = connection.getCredentials();
			User user = connection.showUser(settings.getLogin().getId());
			db.updateCurrentLogin(user);
			return new Result(user, credentials, null);

		} catch (ConnectionException exception) {
			return new Result(null, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Result {

		@Nullable
		public final User user;
		@Nullable
		public final Credentials credentials;
		@Nullable
		public final ConnectionException exception;

		public Result(@Nullable User user, @Nullable Credentials credentials, @Nullable ConnectionException exception) {
			this.exception = exception;
			this.credentials = credentials;
			this.user = user;
		}
	}
}