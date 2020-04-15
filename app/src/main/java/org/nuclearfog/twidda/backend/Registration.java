package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.LoginPage;

import java.lang.ref.WeakReference;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Background task for app login
 */
public class Registration extends AsyncTask<String, Void, String> {

    @Nullable
    private TwitterEngine.EngineException twException;
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
        } catch (TwitterEngine.EngineException twException) {
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
                Toast.makeText(ui.get(), twException.getMessageResource(), LENGTH_SHORT).show();
            } else {
                Toast.makeText(ui.get(), R.string.error_pin_verification, Toast.LENGTH_SHORT).show();
            }
        }
    }
}