package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Context;
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


    public Registration(Context context) {
        ui = new WeakReference<>((LoginPage)context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected Boolean doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        try {
            if( pin.trim().isEmpty() ) {
                redirectionURL = mTwitter.request();
            } else {
                mTwitter.initialize(pin);
                return true;
            }
        } catch(TwitterException e) {
            errorMessage += e.getErrorMessage();
            failure = true;
        }
        catch ( Exception e ) {
            Log.e("Registration", e.getMessage());
            failure = true;
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        LoginPage connect = ui.get();
        if(connect == null)
            return;
        if(success) {
            connect.setResult(Activity.RESULT_OK);
            connect.finish();
        } else if (failure) {
            Toast.makeText(connect,errorMessage,Toast.LENGTH_LONG).show();
        } else {
            connect.connect(redirectionURL);
        }

    }

    public interface OnConnect {
        void connect(String link);
    }
}

