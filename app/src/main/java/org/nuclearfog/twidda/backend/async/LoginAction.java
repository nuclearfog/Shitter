package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Connection;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.api.ConnectionManager;
import org.nuclearfog.twidda.backend.api.twitter.Twitter;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.AppDatabase;
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

	private WeakReference<LoginActivity> weakRef;
	private AccountDatabase accountDB;
	private AppDatabase database;
	private Connection connection;

	@Nullable
	private ConnectionException exception;
	private int mode;

	/**
	 * Account to twitter with PIN
	 *
	 * @param activity Activity Context
	 */
	public LoginAction(LoginActivity activity, int mode) {
		super();
		weakRef = new WeakReference<>(activity);
		accountDB = new AccountDatabase(activity);
		database = new AppDatabase(activity);
		connection = ConnectionManager.get(activity);
		this.mode = mode;
	}


	@Override
	protected String doInBackground(String... param) {
		if (connection instanceof Twitter) {
			try {
				Twitter twitter = (Twitter) connection;
				switch (mode) {
					case MODE_REQUEST:
						return twitter.getRequestToken(param[0], param[1]);

					case MODE_LOGIN:
						// login with pin and access token
						Account account;
						if (param.length == 4)
							account = twitter.login(param[0], param[1], param[2], param[3]);
						else
							account = twitter.login(param[0], param[1]);
						// save new user information
						database.saveUser(account.getUser());
						accountDB.saveLogin(account);
						return "";
				}
			} catch (ConnectionException exception) {
				this.exception = exception;
			}
		}
		return null;
	}


	@Override
	protected void onPostExecute(String result) {
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