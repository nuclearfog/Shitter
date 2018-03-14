package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.TweetPopup;

import java.lang.ref.WeakReference;

public class StatusUpload extends AsyncTask<Object, Void, Boolean> {

    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private ProgressBar load;
    private String[] path;
    private String error;

    /**
     * @param context Context of #TweetPopup
     * @param path Internal Path of the Image
     */
    public StatusUpload(Context context, String[] path) {
        ui = new WeakReference<>((TweetPopup)context);
        mTwitter = TwitterEngine.getInstance(context);
        load = (ProgressBar) ui.get().findViewById(R.id.tweet_sending);
        this.path = path;
    }

    @Override
    protected void onPreExecute() {
        load.setVisibility(View.VISIBLE);
    }

    /**
     * @param args Argument + Text
     *             args[0] = TWEET TEXT
     *             args[1] = REPLY TWEET ID
     */
    @Override
    protected Boolean doInBackground(Object... args) {
        try {
            Long id = -1L;
            String tweet = (String) args[0];
            if(args.length > 1) {
                id = (Long) args[1];
            }
            if(path.length == 0) {
                mTwitter.sendStatus(tweet,id);
            } else {
                mTwitter.sendStatus(tweet,id,path);
            }
            return true;
        } catch(Exception err) {
            error = err.getMessage();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        TweetPopup connect = ui.get();
        if(connect == null)
            return;
        Context context = connect.getApplicationContext();
        if(success) {
            Toast.makeText(context, "gesendet!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Fehler: "+error, Toast.LENGTH_LONG).show();
        }
        connect.finish();
    }
}