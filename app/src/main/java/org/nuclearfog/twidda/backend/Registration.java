package org.nuclearfog.twidda.backend;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import org.nuclearfog.twidda.window.LoginPage;

import java.lang.ref.WeakReference;

public class Registration extends AsyncTask<String, Void, Boolean> {

    private WeakReference<LoginPage> ui;
    private TwitterEngine mTwitter;
    private String errMSG = "";

    /**
     * Register App for Account access
     * @see LoginPage
     * @see TwitterEngine used to Store Twitter instance
     * @param context current Activity's Context.
     */
    public Registration(Context context) {
        ui = new WeakReference<>((LoginPage)context);
        mTwitter = TwitterEngine.getInstance(context);
    }


    @Override
    protected Boolean doInBackground( String... twitterPin ) {
        String pin = twitterPin[0];
        try {
            if( pin.trim().isEmpty() ) {
                mTwitter.request();
            } else {
                mTwitter.initialize(pin);
                return true;
            }
        } catch ( Exception e ) {
            errMSG = e.getMessage();
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
        } else if(!errMSG.isEmpty()) {
            Context context = connect.getApplicationContext();
            Toast.makeText(context,"Fehler: "+errMSG,Toast.LENGTH_LONG).show();
        }
    }
}