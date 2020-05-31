package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.TweetPopup;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TweetHolder;

import java.lang.ref.WeakReference;

/**
 * Background task for uploading tweet
 * @see TweetPopup
 */
public class TweetUploader extends AsyncTask<Void, Void, Boolean> {

    @Nullable
    private EngineException twException;
    private WeakReference<TweetPopup> callback;
    private TwitterEngine mTwitter;
    private TweetHolder tweet;

    /**
     * initialize task
     *
     * @param callback Activity context
     * @param tweet    tweet information
     */
    public TweetUploader(TweetPopup callback, TweetHolder tweet) {
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        this.tweet = tweet;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setLoading(true);
        }
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.uploadStatus(tweet);
            return true;
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            callback.get().setLoading(false);
            if (success) {
                callback.get().onSuccess();
            } else if (twException != null) {
                callback.get().onError(tweet, twException);
            }
        }
    }
}