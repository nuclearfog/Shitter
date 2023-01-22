package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.api.twitter.impl.TwitterAccount;
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
	public static final int MODE_REQUEST = 1;

	/**
	 * login with pin and ans save auth keys
	 */
	public static final int MODE_LOGIN = 2;

	/**
	 * use Twitter account to login
	 */
	public static final int LOGIN_TWITTER_1 = 10;

	/**
	 * use Twitter account to login
	 */
	public static final int LOGIN_TWITTER_2 = 11;

	/**
	 * use Mastodon account to login
	 */
	public static final int LOGIN_MASTODON = 20;

	private WeakReference<LoginActivity> weakRef;
	private AppDatabase database;
	private GlobalSettings settings;
	private Connection connection;
	@Nullable
	private ConnectionException exception;

	private int mode, network;

	/**
	 * Account to twitter with PIN
	 *
	 * @param activity Activity Context
	 * @param network  network type {@link #LOGIN_MASTODON,#LOGIN_TWITTER}
	 * @param mode     indicating login step
	 */
	public LoginAction(LoginActivity activity, int network, int mode) {
		super();
		weakRef = new WeakReference<>(activity);
		database = new AppDatabase(activity);
		settings = GlobalSettings.getInstance(activity);
		this.mode = mode;
		this.network = network;

		if (network == LOGIN_TWITTER_1 || network == LOGIN_TWITTER_2) {
			connection = ConnectionManager.get(activity, ConnectionManager.SELECT_TWITTER);
		} else if (network == LOGIN_MASTODON) {
			connection = ConnectionManager.get(activity, ConnectionManager.SELECT_MASTODON);
		} else {
			throw new RuntimeException("no connection selected: " + mode);
		}
	}


	@Override
	protected String doInBackground(String... param) {
		try {
			switch (mode) {
				case MODE_REQUEST:
					if (settings.isLoggedIn()) {
						Account login = settings.getLogin();
						if (!database.containsLogin(login.getId())) {
							database.saveLogin(login);
						}
					}
					return connection.getAuthorisationLink(param);

				case MODE_LOGIN:
					// login with pin and access token
					Account account = connection.loginApp(param);
					if (network == LOGIN_TWITTER_2 && account instanceof TwitterAccount) {
						((TwitterAccount) account).enableV2();
					}
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