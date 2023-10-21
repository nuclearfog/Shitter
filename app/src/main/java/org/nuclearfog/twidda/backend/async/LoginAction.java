package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.ConnectionResult;
import org.nuclearfog.twidda.backend.helper.update.ConnectionUpdate;
import org.nuclearfog.twidda.backend.helper.update.PushUpdate;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.ui.activities.LoginActivity;

/**
 * Background task to connect to social network
 *
 * @author nuclearfog
 * @see LoginActivity
 */
public class LoginAction extends AsyncExecutor<LoginAction.Param, LoginAction.Result> {

	private AppDatabase database;
	private GlobalSettings settings;
	private ConnectionManager manager;

	/**
	 *
	 */
	public LoginAction(Context context) {
		database = new AppDatabase(context);
		settings = GlobalSettings.get(context);
		manager = ConnectionManager.getInstance(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		Connection connection = manager.getConnection(param.configuration);
		try {
			switch (param.mode) {
				case Param.MODE_REQUEST:
					if (settings.isLoggedIn()) {
						Account login = settings.getLogin();
						if (!database.containsLogin(login.getId())) {
							database.saveLogin(login);
						}
					}
					ConnectionResult result = connection.getAuthorisationLink(param.connection);
					return new Result(Result.MODE_REQUEST, null, result, null);

				case Param.MODE_LOGIN:
					// login with pin and access token
					Account account = connection.loginApp(param.connection, param.code);
					// get instance information
					Instance instance = connection.getInformation();
					// remove old entries to prevent conflicts
					database.resetDatabase();
					// save new user information
					database.saveLogin(account);
					// save instance information
					database.saveInstance(instance);
					// transfer push configuration
					if (settings.pushEnabled()) {
						try {
							connection.updatePush(new PushUpdate(settings.getWebPush()));
						} catch (ConnectionException exception) {
							settings.setPushEnabled(false);
						}
					}
					return new Result(Result.MODE_LOGIN, account, null, null);

				default:
					return null;
			}
		} catch (ConnectionException exception) {
			return new Result(Result.MODE_ERROR, null, null, exception);
		}
	}

	/**
	 *
	 */
	public static class Param {

		public static final int MODE_REQUEST = 1;
		public static final int MODE_LOGIN = 2;

		final ConnectionUpdate connection;
		final Configuration configuration;
		final String code;
		final int mode;

		public Param(int mode, Configuration configuration, ConnectionUpdate connection, String code) {
			this.connection = connection;
			this.configuration = configuration;
			this.mode = mode;
			this.code = code;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int MODE_ERROR = -1;
		public static final int MODE_REQUEST = 3;
		public static final int MODE_LOGIN = 4;

		public final int mode;
		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final ConnectionResult connection;
		@Nullable
		public final Account account;

		Result(int mode, @Nullable Account accout, @Nullable ConnectionResult connection, @Nullable ConnectionException exception) {
			this.connection = connection;
			this.exception = exception;
			this.account = accout;
			this.mode = mode;
		}
	}
}