package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;


public class Registration extends AsyncTask<String, Void, Boolean> {

    public enum Mode {
        LINK,
        LOGIN
    }

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private TwitterException twException;
    private String redirectionURL;
    private Mode mode;


    public Registration(@NonNull LoginPage context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        this.mode = mode;
    }


    @Override
    protected Boolean doInBackground(String... twitterPin) {
        try {
            switch (mode) {
                case LINK:
                    redirectionURL = mTwitter.request();
                    return true;

                case LOGIN:
                    String pin = twitterPin[0];
                    if (!pin.trim().isEmpty())
                        mTwitter.initialize(pin);
                    return true;
            }
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
                switch (mode) {
                    case LINK:
                        ui.get().connect(redirectionURL);
                        break;

                    case LOGIN:
                        ui.get().setResult(Activity.RESULT_OK);
                        ui.get().finish();
                        break;
                }
            } else if (twException != null) {
                ErrorHandler.printError(ui.get(), twException);
            } else {
                Toast.makeText(ui.get(), R.string.pin_verification_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}