package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class Registration extends AsyncTask<String, Void, Boolean> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private boolean failure = false;
    private String errorMessage = "E: Registration, ";
    private String redirectionURL = "";


    public Registration(LoginPage context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected Boolean doInBackground(String... twitterPin) {
        String pin = twitterPin[0];
        try {
            if (pin.trim().isEmpty()) {
                redirectionURL = mTwitter.request();
            } else {
                mTwitter.initialize(pin);
                return true;
            }
        } catch (TwitterException e) {
            errorMessage += e.getMessage();
            failure = true;
        } catch (Exception e) {
            errorMessage += e.getMessage();
            e.printStackTrace();
            Log.e("Registration", errorMessage);
            failure = true;
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            if (success) {
                ui.get().setResult(Activity.RESULT_OK);
                ui.get().finish();
            } else if (failure) {
                Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_LONG).show();
            } else {
                ui.get().connect(redirectionURL);
            }
        }
    }
}