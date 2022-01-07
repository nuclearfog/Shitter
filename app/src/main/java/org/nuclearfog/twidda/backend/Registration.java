package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.LoginActivity;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.User;

import java.lang.ref.WeakReference;

/**
 * Background task to connect to twitter and initialize keys
 *
 * @author nuclearfog
 * @see LoginActivity
 */
public class Registration extends AsyncTask<String, Void, String> {

    private WeakReference<LoginActivity> callback;
    private AccountDatabase accountDB;
    private AppDatabase database;
    private Twitter twitter;
    private GlobalSettings settings;

    /**
     * Account to twitter with PIN
     *
     * @param activity Activity Context
     */
    public Registration(LoginActivity activity) {
        super();
        this.callback = new WeakReference<>(activity);
        // init database and storage
        accountDB = new AccountDatabase(activity);
        database = new AppDatabase(activity);
        settings = GlobalSettings.getInstance(activity);
        twitter = Twitter.get(activity);
    }


    @Override
    protected String doInBackground(String... param) {
        try {
            // check if we need to backup current session
            if (settings.isLoggedIn() && !accountDB.exists(settings.getCurrentUserId())) {
                accountDB.setLogin(settings.getCurrentUserId(), settings.getAccessToken(), settings.getTokenSecret());
            }
            // no PIN means we need to request a token to login
            if (param.length == 0) {
                return twitter.getRequestToken();
            }
            // login with pin
            User user = twitter.login(param[0], param[1]);
            database.storeUser(user);
            return "";
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String redirectionURL) {
        if (callback.get() != null) {
            if (redirectionURL != null) {
                if (!redirectionURL.isEmpty()) {
                    callback.get().connect(redirectionURL);
                } else {
                    callback.get().onSuccess();
                }
            }
        }
    }
}