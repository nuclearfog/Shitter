package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.LoginPage;

public class RegisterAccount extends AsyncTask<String, Void, String> {
    private Context context;

    /**
     * Register App for Account access
     * @see LoginPage
     * @see TwitterEngine used to Store Twitter instance
     * @param context current Activity's Context.
     */
    public RegisterAccount(Context context) {
        this.context = context;
    }


    @Override
    protected void onPreExecute() { }


    @Override
    protected String doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        TwitterEngine mTwitter = TwitterEngine.getInstance(context);
        try {
            if( pin.trim().isEmpty() ) {
                mTwitter.request();
            }else {
                mTwitter.initialize(pin);
                return "success";
            }
        } catch ( Exception e ) {
            return e.getMessage();
        }
        return " ";
    }


    @Override
    protected void onPostExecute(String msg) {
        if( msg.equals("success") ) {
            ((LoginPage)context).setResult(Activity.RESULT_OK);
            ((LoginPage)context).finish();
        } else if( !msg.trim().isEmpty() ) {
            Toast.makeText(context,"Fehler: "+msg,Toast.LENGTH_LONG).show();
        }
    }
}