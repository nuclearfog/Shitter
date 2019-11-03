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
public class Registration extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private TwitterException twException;
    private String redirectionURL, loginPin;


    /**
     * Get Twitter redirection URL
     *
     * @param context Activity Context
     */
    public Registration(LoginPage context) {
        this(context, null);
    }


    /**
     * Login to twitter with PIN
     *
     * @param context  Activity Context
     * @param loginPin PIN from twitter website
     */
    public Registration(LoginPage context, String loginPin) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        this.loginPin = loginPin;
    }


    @Override
    protected Boolean doInBackground(Void... v) {
        try {
            if (loginPin == null)
                redirectionURL = mTwitter.request();
            else
                mTwitter.initialize(loginPin);
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            if (success) {
                if (redirectionURL != null) {
                    ui.get().connect(redirectionURL);
                } else if (loginPin != null) {
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