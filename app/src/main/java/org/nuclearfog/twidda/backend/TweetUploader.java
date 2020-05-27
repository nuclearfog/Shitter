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
    private WeakReference<TweetPopup> ui;
    private TwitterEngine mTwitter;
    private TweetHolder tweet;

    /**
     * initialize task
     *
     * @param context Activity context
     * @param tweet   tweet information
     */
    public TweetUploader(TweetPopup context, TweetHolder tweet) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        this.tweet = tweet;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null) {
            ui.get().setLoading(true);
        }
    }


    @Override
    protected Boolean doInBackground(Void[] v) {
        try {
            mTwitter.uploadStatus(tweet);
        } catch (EngineException twException) {
            this.twException = twException;
            return false;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() != null) {
            ui.get().setLoading(false);
            if (success) {
                ui.get().onSuccess();
            } else if (twException != null) {
                ui.get().onError(tweet, twException);
            }
        }
    }
}