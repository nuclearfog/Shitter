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
    private WeakReference<TweetEditor> weakRef;

    /**
     * initialize task
     *
     * @param activity Activity context
     */
    public TweetUpdater(TweetEditor activity) {
        super();
        twitter = Twitter.get(activity);
        weakRef = new WeakReference<>(activity);
    }


    @Override
    protected Boolean doInBackground(TweetUpdate... tweets) {
        TweetUpdate update = tweets[0];
        try {
            // upload media first
            MediaStream[] mediaStreams = update.getMediaStreams();
            long[] mediaIds = new long[mediaStreams.length];
            for (int pos = 0; pos < mediaStreams.length; pos++) {
                // upload media file and save media ID
                mediaIds[pos] = twitter.uploadMedia(mediaStreams[pos]);
            }
            // upload tweet
            if (!isCancelled()) {
                twitter.uploadTweet(update, mediaIds);
            }
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        } finally {
            // close inputstreams
            update.close();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        TweetEditor activity = weakRef.get();
        if (activity != null) {
            if (success) {
                activity.onSuccess();
            } else {
                activity.onError(twException);
            }
        }
    }
}