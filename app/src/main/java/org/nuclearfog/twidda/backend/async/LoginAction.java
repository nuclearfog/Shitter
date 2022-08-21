package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.LoginActivity;

import java.lang.ref.WeakReference;

/**
 * Background task to connect to twitter and initialize keys
 *
 * @author nuclearfog
 * @see LoginActivity
 */
public class LoginAction extends AsyncTask<String, Void, String> {

	@Nullable
	private ErrorHandler.TwitterError exception;
	private WeakReference<LoginActivity> weakRef;
	private AccountDatabase accountDB;
	private AppDatabase database;
	private Twitter twitter;
	private GlobalSettings settings;

	/**
	 * Account to twitter with PIN
	 *
	 * @param activity Activity Context
	 */
	public LoginAction(LoginActivity activity) {
		super();
		weakRef = new WeakReference<>(activity);
		accountDB = new AccountDatabase(activity);
		database = new AppDatabase(activity);
		settings = GlobalSettings.getInstance(activity);
		twitter = Twitter.get(activity);
	}


	@Override
	protected String doInBackground(String... param) {
		try {
			// no PIN means we need to request a token to login
			if (param.length == 0) {
				// backup current login if exist
				if (settings.isLoggedIn() && !accountDB.exists(settings.getCurrentUserId())) {
					accountDB.setLogin(settings.getCurrentUserId(), settings.getAccessToken(), settings.getTokenSecret());
				}
				return twitter.getRequestToken();
			}
			// login with pin and access token
			User user = twitter.login(param[0], param[1]);
			// save new user information
			database.storeUser(user);
			accountDB.setLogin(user.getId(), settings.getAccessToken(), settings.getTokenSecret());
			return "";
		} catch (TwitterException exception) {
			this.exception = exception;
			return null;
		}
	}


	@Override
	protected void onPostExecute(String result) {
		LoginActivity activity = weakRef.get();
		if (activity != null) {
			// redirect to Twitter login page
			if (result != null) {
				if (result.isEmpty()) {
					activity.onSuccess();
				} else {
					activity.connect(result);
				}
			} else {
				activity.onError(exception);
			}
		}
	}
}