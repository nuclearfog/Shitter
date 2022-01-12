package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.TweetEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.holder.TweetHolder;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<Void, Void, Boolean> {


    private ErrorHandler.TwitterError twException;
    private Twitter twitter;
    private WeakReference<TweetEditor> callback;
    private TweetHolder tweet;

    /**
     * initialize task
     *
     * @param activity Activity context
     */
    public TweetUpdater(TweetEditor activity, TweetHolder tweet) {
        super();
        twitter = Twitter.get(activity);
        callback = new WeakReference<>(activity);
        this.tweet = tweet;
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            long[] mediaIds = {};
            String[] mediaLinks = tweet.getMediaPaths();
            if (mediaLinks != null && mediaLinks.length > 0) {
                mediaIds = new long[mediaLinks.length];

                // upload image
                if (tweet.getMediaType() == TweetHolder.MediaType.IMAGE) {
                    for (int i = 0; i < mediaLinks.length; i++) {
                        mediaIds[i] = twitter.uploadImage(mediaLinks[i]);
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                // upload video file
                else if (tweet.getMediaType() == TweetHolder.MediaType.VIDEO) {// fixme
                    //mediaIds[0] = mTwitter.uploadVideo(mediaLinks[0]);
                }
            }
            // upload tweet
            if (!isCancelled()) {
                double[] coordinates = null;
                if (tweet.hasLocation())
                    coordinates = new double[] {tweet.getLongitude(), tweet.getLatitude()};
                twitter.uploadTweet(tweet.getText(), tweet.getReplyId(), mediaIds, coordinates);
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