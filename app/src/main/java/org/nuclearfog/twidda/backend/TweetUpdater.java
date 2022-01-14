package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.TweetEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.MediaStream;
import org.nuclearfog.twidda.backend.api.holder.TweetUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<Void, Void, Boolean> {

    private Twitter twitter;
    private ErrorHandler.TwitterError twException;
    private WeakReference<TweetEditor> callback;
    private TweetUpdate tweet;

    /**
     * initialize task
     *
     * @param activity Activity context
     */
    public TweetUpdater(TweetEditor activity, TweetUpdate tweet) {
        super();
        twitter = Twitter.get(activity);
        callback = new WeakReference<>(activity);
        this.tweet = tweet;
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            long[] mediaIds = {};
            String[] mimeTypes = tweet.getMimeTypes();
            InputStream[] mediaStreams = tweet.getMediaStreams();
            if (mimeTypes != null && mediaStreams != null) {
                // upload media first
                mediaIds = new long[mediaStreams.length];
                for (int pos = 0; pos < mediaStreams.length; pos++) {
                    // upload media file and save media ID
                    MediaStream mediaStream = new MediaStream(mediaStreams[pos], mimeTypes[pos]);
                    mediaIds[pos] = twitter.uploadMedia(mediaStream);
                    // close stream after upload
                    mediaStreams[pos].close();
                }
            }
            // upload tweet
            if (!isCancelled()) {
                twitter.uploadTweet(tweet, mediaIds);
            }
            return true;
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
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