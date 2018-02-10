package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import org.nuclearfog.twidda.window.LoginPage;

public class RegisterAccount extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private String errMSG = "";

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
    protected Boolean doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        TwitterEngine mTwitter = TwitterEngine.getInstance(context);
        try {
            if( pin.trim().isEmpty() ) {
                mTwitter.request();
            }else {
                mTwitter.initialize(pin);
                return true;
            }
        } catch ( Exception e ) {
            errMSG =  e.getMessage();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if(success) {
            ((LoginPage)context).setResult(Activity.RESULT_OK);
            ((LoginPage)context).finish();
        } else if(errMSG.isEmpty()) {
            Toast.makeText(context,"Fehler: "+errMSG,Toast.LENGTH_LONG).show();
        }
    }
}