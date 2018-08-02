package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class Registration extends AsyncTask<String, Void, Boolean> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private ErrorLog errorLog;
    private String errorMessage;
    private String redirectionURL = "";


    public Registration(Context context) {
        ui = new WeakReference<>((LoginPage)context);
        mTwitter = TwitterEngine.getInstance(context);
        errorLog = new ErrorLog(context);
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
            errorMessage = "E: " + e.getErrorMessage();
            errorLog.add(errorMessage);
        }
        catch ( Exception e ) {
            errorMessage = "E: Registration, " + e.getMessage();
            errorLog.add(errorMessage);
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
        } else if (errorMessage != null) {
            Toast.makeText(connect,errorMessage,Toast.LENGTH_LONG).show();
        } else {
            ui.get().connect(redirectionURL);
        }

    }

    public interface OnConnect {
        void connect(String link);
    }
}

