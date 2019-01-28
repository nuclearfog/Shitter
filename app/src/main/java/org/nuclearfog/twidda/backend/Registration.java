package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;


public class Registration extends AsyncTask<String, Void, Boolean> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private boolean failure = false;
    private String redirectionURL = "";


    public Registration(@NonNull LoginPage context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected Boolean doInBackground(String... twitterPin) {
        String pin = twitterPin[0];
        try {
            redirectionURL = mTwitter.request();
            if (!pin.trim().isEmpty()) {
                mTwitter.initialize(pin);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Registration", e.getMessage());
            failure = true;
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        if (success) {
            ui.get().setResult(Activity.RESULT_OK);
            ui.get().finish();
        } else if (failure) {
            Toast.makeText(ui.get(), R.string.pin_verification_failed, Toast.LENGTH_SHORT).show();
        } else {
            ui.get().connect(redirectionURL);
        }
    }
}