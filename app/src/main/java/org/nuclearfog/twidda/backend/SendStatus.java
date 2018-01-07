package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;

public class SendStatus extends AsyncTask<Object, Void, Boolean> {

    public static final String SEND_STATUS="stats";

    private Context context;
    private Twitter twitter;

    public SendStatus(Context context) {
        this.context = context;
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
            if(args.length > 1)
                mStatus.setInReplyToStatusId((Long)args[1]);

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