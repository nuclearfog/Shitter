package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.TweetEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.TweetHolder;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 *
 * @author nuclearfog
 * @see TweetEditor
 */
public class TweetUpdater extends AsyncTask<Void, Void, Boolean> {


    private EngineException twException;
    private final TwitterEngine mTwitter;

    private final WeakReference<TweetEditor> callback;
    private TweetHolder tweet;

    /**
     * initialize task
     *
     * @param callback Activity context
     */
    public TweetUpdater(TweetEditor callback, TweetHolder tweet) {
        super();
        mTwitter = TwitterEngine.getInstance(callback);
        this.callback = new WeakReference<>(callback);
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
                        mediaIds[i] = mTwitter.uploadImage(mediaLinks[i]);
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                // upload video file
                else if (tweet.getMediaType() == TweetHolder.MediaType.VIDEO) {
                    mediaIds[0] = mTwitter.uploadVideo(mediaLinks[0]);
                }
            }
            // upload tweet
            if (!isCancelled()) {
                mTwitter.uploadStatus(tweet, mediaIds);
            }
            return true;
        } catch (EngineException twException) {
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