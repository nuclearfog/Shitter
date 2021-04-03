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
public class TweetUpdater extends AsyncTask<TweetHolder, Void, Boolean> {


    private EngineException twException;
    private final WeakReference<TweetEditor> callback;
    private final TwitterEngine mTwitter;

    /**
     * initialize task
     *
     * @param callback Activity context
     */
    public TweetUpdater(TweetEditor callback) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
    }


    @Override
    protected Boolean doInBackground(TweetHolder[] param) {
        try {
            long[] mediaIds;
            TweetHolder tweet = param[0];
            if (tweet.getMediaType() == TweetHolder.MediaType.IMAGE) {
                String[] mediaLinks = tweet.getMediaPaths();
                mediaIds = new long[mediaLinks.length];
                for (int i = 0; i < mediaLinks.length; i++) {
                    mediaIds[i] = mTwitter.uploadImage(mediaLinks[i]);
                    if (isCancelled()) {
                        break;
                    }
                }
            } else if (tweet.getMediaType() == TweetHolder.MediaType.VIDEO) {
                mediaIds = new long[]{mTwitter.uploadVideo(tweet.getMediaPath())};
            } else {
                mediaIds = new long[0];
            }
            if (!isCancelled()) {
                mTwitter.uploadStatus(tweet, mediaIds);
            }
            return true;
        } catch (EngineException twException) {
            this.twException = twException;
            return false;
        }
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