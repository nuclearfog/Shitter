package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;

public class SendStatus extends AsyncTask<Object, Void, Boolean> {

    public static final String SEND_STATUS="stats";

    private Context context;
    private Twitter twitter;
    private String path;

    /**
     * @param context Context of #TweetPopup
     * @param path Internal Path of the Image
     */
    public SendStatus(Context context, String path) {
        this.context = context;
        this.path = path;
        twitter = TwitterResource.getInstance(context).getTwitter();
    }

    /**
     * @param args Argument + Text
     *             args[0] = TWEET TEXT
     *             args[1] = REPLY TWEET ID
     */
    @Override
    protected Boolean doInBackground(Object... args) {
        try {
            String tweet = (String) args[0];
            StatusUpdate mStatus = new StatusUpdate(tweet);
            if(args.length > 1) { //ANSWER TO USER
                mStatus.setInReplyToStatusId((Long)args[1]);
            }
            if(!path.trim().isEmpty()) { //ADD IMAGE
                mStatus.setMedia(new File(path));
            }
            twitter.tweets().updateStatus(mStatus);
            return true;
        } catch(Exception err) {
            err.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(success) {
            Toast.makeText(context, "Tweet wurde gesendet!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Fehler beim Senden des Tweets!", Toast.LENGTH_LONG).show();
        }
    }
}