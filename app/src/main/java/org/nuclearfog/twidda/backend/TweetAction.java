package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.TweetActivity;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;


/**
 * Background task to download tweet informations and to take actions
 *
 * @author nuclearfog
 * @see TweetActivity
 */
public class TweetAction extends AsyncTask<TweetAction.Action, Tweet, TweetAction.Action> {

    /**
     * actions for the tweet
     */
    public enum Action {
        /**
         * Load tweet
         */
        LOAD,
        /**
         * load tweet from database first
         */
        LD_DB,
        /**
         * retweet tweet
         */
        RETWEET,
        /**
         * remove retweet
         */
        UNRETWEET,
        /**
         * favorite tweet
         */
        FAVORITE,
        /**
         * remove tweet from favorites
         */
        UNFAVORITE,
        /**
         * delete own tweet
         */
        DELETE
    }

    @Nullable
    private EngineException twException;
    private TwitterEngine mTwitter;
    private WeakReference<TweetActivity> callback;
    private AppDatabase db;
    private long tweetId;


    /**
     * @param callback Callback to return tweet information
     * @param tweetId  ID of the tweet
     */
    public TweetAction(TweetActivity callback, long tweetId) {
        super();
        db = new AppDatabase(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        this.callback = new WeakReference<>(callback);
        this.tweetId = tweetId;
    }


    @Override
    protected Action doInBackground(Action[] action) {
        try {
            switch (action[0]) {
                case LD_DB:
                    Tweet tweet = db.getStatus(tweetId);
                    if (tweet != null) {
                        publishProgress(tweet);
                    }

                case LOAD:
                    tweet = mTwitter.getStatus(tweetId);
                    publishProgress(tweet);
                    if (db.containStatus(tweetId)) {
                        // update tweet if there is a database entry
                        db.updateStatus(tweet);
                    }
                    break;

                case DELETE:
                    tweet = mTwitter.deleteTweet(tweetId);
                    db.removeStatus(tweetId);
                    // removing retweet reference to this tweet
                    if (tweet.getMyRetweetId() > 0)
                        db.removeStatus(tweet.getMyRetweetId());
                    break;

                case RETWEET:
                    tweet = mTwitter.retweet(tweetId, true);
                    publishProgress(tweet);
                    db.updateStatus(tweet);
                    break;

                case UNRETWEET:
                    tweet = mTwitter.retweet(tweetId, false);
                    publishProgress(tweet);
                    db.updateStatus(tweet);
                    // removing retweet reference to this tweet
                    if (tweet.getMyRetweetId() > 0)
                        db.removeStatus(tweet.getMyRetweetId());
                    break;

                case FAVORITE:
                    tweet = mTwitter.favorite(tweetId, true);
                    publishProgress(tweet);
                    db.storeFavorite(tweet);
                    break;

                case UNFAVORITE:
                    tweet = mTwitter.favorite(tweetId, false);
                    publishProgress(tweet);
                    db.removeFavorite(tweet);
                    break;
            }
            return action[0];
        } catch (EngineException twException) {
            this.twException = twException;
            if (twException.resourceNotFound()) {
                db.removeStatus(tweetId);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Tweet[] tweets) {
        if (callback.get() != null) {
            callback.get().setTweet(tweets[0]);
        }
    }


    @Override
    protected void onPostExecute(Action action) {
        if (callback.get() != null) {
            if (action != null) {
                callback.get().onAction(action, tweetId);
            } else {
                callback.get().onError(twException, tweetId);
            }
        }
    }
}