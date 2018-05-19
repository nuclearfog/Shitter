package org.nuclearfog.twidda.backend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.window.TweetPopup;

import java.lang.ref.WeakReference;

public class StatusUpload extends AsyncTask<Object, Void, Boolean> implements AlertDialog.OnClickListener {

    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private String[] path;

    /**
     * @param context Context of #TweetPopup
     * @param path Internal Path of the Image
     */
    public StatusUpload(Context context, String[] path) {
        ui = new WeakReference<>((TweetPopup)context);
        mTwitter = TwitterEngine.getInstance(context);

        this.path = path;
    }

    @Override
    protected void onPreExecute() {
        EditText tweet = ui.get().findViewById(R.id.tweet_input);
        View load = ui.get().findViewById(R.id.tweet_sending);
        load.setVisibility(View.VISIBLE);
        tweet.setFocusable(false);
    }

    /**
     * @param args Argument + Text
     *             args[0] = TWEET TEXT String
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
            ErrorLog errorLog = new ErrorLog(ui.get());
            errorLog.add(err.getMessage());
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
            connect.finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ui.get());
            builder.setTitle("Fehler").setMessage("Tweet wurde nicht gesendet!")
                    .setPositiveButton(R.string.retry, this)
                    .setNegativeButton(R.string.cancel, this).show();
            View load = ui.get().findViewById(R.id.tweet_sending);
            load.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        TweetPopup tweetPopup = ui.get();
        switch(id) {
            case DialogInterface.BUTTON_POSITIVE:
                if(tweetPopup != null)
                    tweetPopup.send();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if(tweetPopup != null)
                    tweetPopup.finish();
                break;
        }
    }

    public interface TweetSender {
        void send();
    }
}