package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.activities.LoginActivity;

/**
 * Background task to connect to social network
 *
 * @author nuclearfog
 * @see LoginActivity
 */
public class LoginAction extends AsyncExecutor<LoginAction.LoginParam, LoginAction.LoginResult> {

	private AppDatabase database;
	private GlobalSettings settings;
	private ConnectionManager manager;

	/**
	 *
	 */
	public LoginAction(Context context) {
		database = new AppDatabase(context);
		settings = GlobalSettings.getInstance(context);
		manager = ConnectionManager.getInstance(context);
	}


	@NonNull
	@Override
	protected LoginResult doInBackground(@NonNull LoginParam param) {
		Connection connection = manager.getConnection(param.configuration);
		try {
			switch (param.mode) {
				case LoginParam.MODE_REQUEST:
					if (settings.isLoggedIn()) {
						Account login = settings.getLogin();
						if (!database.containsLogin(login.getId())) {
							database.saveLogin(login);
						}
					}
					String redirectUrl = connection.getAuthorisationLink(param.connection);
					return new LoginResult(LoginResult.MODE_REQUEST, redirectUrl, null);

				case LoginParam.MODE_LOGIN:
					// login with pin and access token
					Account account = connection.loginApp(param.connection, param.code);
					// save new user information
					database.saveLogin(account);
					return new LoginResult(LoginResult.MODE_LOGIN, null, null);
			}
		} catch (ConnectionException exception) {
			return new LoginResult(LoginResult.MODE_ERROR, null, exception);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new LoginResult(LoginResult.MODE_ERROR, null, null);
	}

	/**
	 *
	 */
	public static class LoginParam {

		public static final int MODE_REQUEST = 1;
		public static final int MODE_LOGIN = 2;

		public final ConnectionConfig connection;
		public final Configuration configuration;
		public final String code;
		public final int mode;

		public LoginParam(int mode, Configuration configuration, ConnectionConfig connection, String code) {
			this.connection = connection;
			this.configuration = configuration;
			this.mode = mode;
			this.code = code;
		}
	}

	/**
	 *
	 */
	public static class LoginResult {

		public static final int MODE_ERROR = -1;
		public static final int MODE_REQUEST = 3;
		public static final int MODE_LOGIN = 4;

		public final int mode;
		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final String redirectUrl;

		LoginResult(int mode, @Nullable String redirectUrl, @Nullable ConnectionException exception) {
			this.redirectUrl = redirectUrl;
			this.exception = exception;
			this.mode = mode;
		}
	}
}