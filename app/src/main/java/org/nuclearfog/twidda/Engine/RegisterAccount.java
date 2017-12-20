package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class RegisterAccount extends AsyncTask<String, Void, Boolean> {

    private final String TWITTER_CONSUMER_KEY = "GrylGIgQK3cDjo9mSTBqF1vwf";
    private final String TWITTER_CONSUMER_SECRET = "pgaWUlDVS5b7Q6VJQDgBzHKw0mIxJIX0UQBcT1oFJEivsCl5OV";


    private TwitterStore mTwitter;

    private Button loginButton, verifierButton;
    private SharedPreferences einstellungen;
    private Context context;

    public RegisterAccount( Context context ){
        this.context = context;
        einstellungen = context.getSharedPreferences("settings", 0);
    }

    public void setButton( Button loginButton, Button verifierButton ) {
        this.loginButton=loginButton;
        this.verifierButton=verifierButton;
    }

    @Override
    protected Boolean doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        mTwitter = TwitterStore.getInstance(context);
        try {
            if( pin.trim().isEmpty() ) {
                mTwitter.request();   //check
            }else {
                mTwitter.initialize(pin);
                return true;
            }
        } catch ( TwitterException e ) {
            Toast.makeText(context,"Fehler bei der Registrierung",Toast.LENGTH_LONG).show();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if( result ) {
            loginButton.setVisibility(Button.VISIBLE);
            verifierButton.setVisibility(Button.INVISIBLE);
        }
    }
}