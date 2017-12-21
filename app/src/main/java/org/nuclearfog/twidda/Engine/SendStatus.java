package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;


public class SendStatus extends AsyncTask<String, Void, Boolean> {

    public static final String SEND_STATUS="stats";

    private Context context;

    public SendStatus(Context context){
        this.context = context;
    }


    /**
     * @param args Argument + Text
     *             args[0] = Mode
     *             args[1] = Data
     */
    @Override
    protected Boolean doInBackground(String... args){
        try {
            switch(args[0]) {
                case(SEND_STATUS):
                    String tweet = args[1];
                    TwitterStore mTwitter = TwitterStore.getInstance(context);
                    mTwitter.init();
                    Twitter twitter = mTwitter.getTwitter();
                    twitter.tweets().updateStatus(tweet);
                    return true;
            }
        }catch(TwitterException err){}
        catch(Exception err){}
        return false;
    }

    @Override
    protected void onPostExecute(Boolean success){
        if(success) {
            Toast.makeText(context, "Tweet wurde gesendet!", Toast.LENGTH_LONG).show();
        }
    }
}
