package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.TweetEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.MediaStream;
import org.nuclearfog.twidda.backend.api.holder.TweetUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<TweetUpdate, Void, Boolean> {

    private Twitter twitter;
    private ErrorHandler.TwitterError twException;
    private WeakReference<TweetEditor> callback;

    /**
     * initialize task
     *
     * @param activity Activity context
     */
    public TweetUpdater(TweetEditor activity) {
        super();
        twitter = Twitter.get(activity);
        callback = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(TweetUpdate... tweets) {
        try {
            // upload media first
            MediaStream[] mediaStreams = tweets[0].getMediaStreams();
            long[] mediaIds = new long[mediaStreams.length];
            for (int pos = 0; pos < mediaStreams.length; pos++) {
                // upload media file and save media ID
                mediaIds[pos] = twitter.uploadMedia(mediaStreams[pos]);
            }
            // upload tweet
            if (!isCancelled()) {
                twitter.uploadTweet(tweets[0], mediaIds);
            }
            // close all mediastreams
            for (MediaStream mediaStream : mediaStreams) {
                mediaStream.close();
            }
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(twException);
            }
        }
    }
}