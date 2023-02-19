package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.helper.ConnectionConfig;
import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
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
	private Connection connection;


	public LoginAction(Context context, Configuration configuration) {
		database = new AppDatabase(context);
		settings = GlobalSettings.getInstance(context);
		connection = ConnectionManager.get(context, configuration);
	}


	@NonNull
	@Override
	protected LoginResult doInBackground(LoginParam param) {
		try {
			switch (param.mode) {
				case LoginParam.MODE_REQUEST:
					if (settings.isLoggedIn()) {
						Account login = settings.getLogin();
						if (!database.containsLogin(login.getId())) {
							database.saveLogin(login);
						}
					}
					String redirectUrl = connection.getAuthorisationLink(param.config);
					return new LoginResult(LoginResult.MODE_REQUEST, redirectUrl, null);

				case LoginParam.MODE_LOGIN:
					// login with pin and access token
					Account account = connection.loginApp(param.config, param.code);
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


	public static class LoginParam {

		/**
		 * request login page
		 */
		public static final int MODE_REQUEST = 1;

		/**
		 * request login access
		 */
		public static final int MODE_LOGIN = 2;

		public final ConnectionConfig config;
		public final String code;
		public final int mode;

		public LoginParam(int mode, ConnectionConfig config) {
			this(mode, config, "");
		}

		public LoginParam(int mode, ConnectionConfig config, String code) {
			this.mode = mode;
			this.config = config;
			this.code = code;
		}
	}


	public static class LoginResult {

		public static final int MODE_REQUEST = 3;

		public static final int MODE_LOGIN = 4;

		public static final int MODE_ERROR = -1;

		@Nullable
		public final ConnectionException exception;
		@Nullable
		public final String redirectUrl;
		public final int mode;

		LoginResult(int mode, @Nullable String redirectUrl, @Nullable ConnectionException exception) {
			this.redirectUrl = redirectUrl;
			this.exception = exception;
			this.mode = mode;
		}
	}
}