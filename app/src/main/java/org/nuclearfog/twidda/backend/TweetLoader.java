package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;


/**
 * Background task to download tweet informations and to take actions
 * @see TweetDetail
 */
public class TweetLoader extends AsyncTask<Long, Tweet, Tweet> {

    public enum Action {
        LOAD,
        RETWEET,
        FAVORITE,
        DELETE
    }

    @Nullable
    private EngineException twException;
    private TwitterEngine mTwitter;
    private WeakReference<TweetDetail> callback;
    private AppDatabase db;
    private final Action action;


    public TweetLoader(TweetDetail callback, Action action) {
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
        this.callback = new WeakReference<>(callback);
        this.action = action;
    }


    @Override
    protected Tweet doInBackground(Long[] data) {
        Tweet tweet = null;
        long tweetId = data[0];
        boolean updateStatus = false;
        try {
            switch (action) {
                case LOAD:
                    tweet = db.getStatus(tweetId);
                    if (tweet != null) {
                        publishProgress(tweet);
                        updateStatus = true;
                    }
                    tweet = mTwitter.getStatus(tweetId);
                    publishProgress(tweet);
                    if (updateStatus)
                        db.updateStatus(tweet);
                    break;

                case DELETE:
                    tweet = mTwitter.deleteTweet(tweetId);
                    db.removeStatus(tweetId);
                    break;

                case RETWEET:
                    tweet = mTwitter.retweet(tweetId);
                    publishProgress(tweet);

                    if (!tweet.retweeted())
                        db.removeRetweet(tweetId);
                    db.updateStatus(tweet);
                    break;

                case FAVORITE:
                    tweet = mTwitter.favorite(tweetId);
                    publishProgress(tweet);

                    if (tweet.favored())
                        db.storeFavorite(tweet);
                    else
                        db.removeFavorite(tweetId);
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
            if (twException.resourceNotFound()) {
                db.removeStatus(tweetId);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return tweet;
    }


    @Override
    protected void onProgressUpdate(Tweet[] tweets) {
        Tweet tweet = tweets[0];
        if (callback.get() != null && tweet != null) {
            callback.get().setTweet(tweet);
        }
    }


    @Override
    protected void onPostExecute(Tweet tweet) {
        if (callback.get() != null) {
            if (tweet != null) {
                callback.get().onAction(tweet, action);
            }
            if (twException != null) {
                callback.get().onError(twException);
            }
        }
    }
}