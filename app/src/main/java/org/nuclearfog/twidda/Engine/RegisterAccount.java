package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;
import twitter4j.TwitterException;

public class RegisterAccount extends AsyncTask<String, Void, Boolean>
{
    private TwitterStore mTwitter;
    private Button loginButton, verifierButton;
    private Context context;

    public RegisterAccount( Context context ){
        this.context = context;
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