package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.update.ConnectionConfig;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.activities.LoginActivity;

import java.lang.ref.WeakReference;

/**
 * Background task to connect to social network
 *
 * @author nuclearfog
 * @see LoginActivity
 */
public class LoginAction extends AsyncTask<String, Void, String> {

	/**
	 * request login page
	 */
	public static final int MODE_REQUEST_TWITTER = 1;

	/**
	 * request login page
	 */
	public static final int MODE_REQUEST_MASTODON = 2;

	/**
	 * login with pin and ans save auth keys
	 */
	public static final int MODE_LOGIN_TWITTER = 3;

	/**
	 * use Mastodon account to login
	 */
	public static final int MODE_LOGIN_MASTODON = 4;

	private WeakReference<LoginActivity> weakRef;
	private AppDatabase database;
	private GlobalSettings settings;
	private Connection connection;
	@Nullable
	private ConnectionException exception;

	private ConnectionConfig configuration;
	private int mode;

	/**
	 * Account to twitter with PIN
	 *
	 * @param activity      Activity Context
	 * @param configuration network type {@link #MODE_LOGIN_MASTODON ,#LOGIN_TWITTER}
	 * @param mode          indicating login step
	 */
	public LoginAction(LoginActivity activity, int mode, ConnectionConfig configuration) {
		super();
		weakRef = new WeakReference<>(activity);
		database = new AppDatabase(activity);
		settings = GlobalSettings.getInstance(activity);
		this.configuration = configuration;
		this.mode = mode;
		switch (mode) {
			case MODE_REQUEST_TWITTER:
			case MODE_LOGIN_TWITTER:
				if (configuration.getApiType() == ConnectionConfig.API_TWITTER_2)
					connection = ConnectionManager.get(activity, ConnectionManager.SELECT_TWITTER_2);
				else
					connection = ConnectionManager.get(activity, ConnectionManager.SELECT_TWITTER_1);
				break;

			case MODE_REQUEST_MASTODON:
			case MODE_LOGIN_MASTODON:
				connection = ConnectionManager.get(activity, ConnectionManager.SELECT_MASTODON);
				break;

			default:
				throw new RuntimeException("connection type not found: " + mode);
		}
	}


	@Override
	protected String doInBackground(String... params) {
		try {
			switch (mode) {
				case MODE_REQUEST_TWITTER:
				case MODE_REQUEST_MASTODON:
					if (settings.isLoggedIn()) {
						Account login = settings.getLogin();
						if (!database.containsLogin(login.getId())) {
							database.saveLogin(login);
						}
					}
					return connection.getAuthorisationLink(configuration);

				case MODE_LOGIN_TWITTER:
				case MODE_LOGIN_MASTODON:
					// login with pin and access token
					Account account = connection.loginApp(configuration, params[0], params[1]);
					// save new user information
					database.saveLogin(account);
					return "";
			}
		} catch (ConnectionException exception) {
			this.exception = exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable String result) {
		LoginActivity activity = weakRef.get();
		if (activity != null) {
			if (result != null) {
				activity.onSuccess(mode, result);
			} else {
				activity.onError(exception);
			}
		}
	}
}