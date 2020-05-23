package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.LoginPage;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;

import java.lang.ref.WeakReference;

/**
 * Background task to connect to twitter and initialize keys
 * @see LoginPage
 */
public class Registration extends AsyncTask<String, Void, String> {

    @Nullable
    private EngineException twException;
    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;

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
        } catch (EngineException twException) {
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
                    ui.get().onSuccess();
                }
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }
}