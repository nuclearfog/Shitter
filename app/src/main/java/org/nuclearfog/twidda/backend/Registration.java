package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

/**
 * Background task for app login
 */
public class Registration extends AsyncTask<String, Void, String> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private TwitterException twException;

    /**
     * Login to twitter with PIN
     *
     * @param context  Activity Context
     */
    public Registration(LoginPage context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected String doInBackground(String... param) {
        try {
            if (param.length == 0)
                return mTwitter.request();
            mTwitter.initialize(param[0]);
            return "";
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String redirectionURL) {
        if (ui.get() != null) {
            if (redirectionURL != null) {
                if (!redirectionURL.isEmpty()) {
                    ui.get().connect(redirectionURL);
                } else {
                    ui.get().setResult(Activity.RESULT_OK);
                    ui.get().finish();
                }
            } else if (twException != null) {
                ErrorHandler.printError(ui.get(), twException);
            } else {
                Toast.makeText(ui.get(), R.string.pin_verification_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}