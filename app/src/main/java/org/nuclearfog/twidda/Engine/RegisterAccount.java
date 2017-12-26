package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;

import twitter4j.TwitterException;

public class RegisterAccount extends AsyncTask<String, Void, String>
{
    private Button verifierButton, loginButton;
    private Context context;

    public RegisterAccount( Context context ){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        verifierButton  = (Button)((MainActivity)context).findViewById(R.id.verifier);
        loginButton = (Button)((MainActivity)context).findViewById(R.id.login);
    }

    @Override
    protected String doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        TwitterStore mTwitter = TwitterStore.getInstance(context);
        try {
            if( pin.trim().isEmpty() ) {
                mTwitter.request();
            }else {
                mTwitter.initialize(pin);
                return "success";
            }
        } catch ( TwitterException e ) {
            return e.getMessage();
        } catch ( Exception e ) {
            return e.getMessage();
        }
        return " ";
    }

    @Override
    protected void onPostExecute(String msg) {
        if( msg=="success" ) {
            verifierButton.setVisibility(Button.INVISIBLE);
            loginButton.setVisibility(Button.VISIBLE);
            loginButton.setBackgroundColor(0xFFFF0000);//todo
        } else if( !msg.trim().isEmpty() ) {
            Toast.makeText(context,"Fehler: "+msg,Toast.LENGTH_LONG).show();
        }
    }
}