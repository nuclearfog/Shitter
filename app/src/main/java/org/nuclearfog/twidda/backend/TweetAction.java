package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.TweetActivity;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Tweet;

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
    private TwitterException twException;
    private Twitter twitter;
    private WeakReference<TweetActivity> callback;
    private AppDatabase db;
    private long tweetId, retweetId;


    /**
     * @param tweetId ID of the tweet
     */
    public TweetAction(TweetActivity activity, long tweetId, long retweetId) {
        super();
        db = new AppDatabase(activity);
        twitter = Twitter.get(activity);
        callback = new WeakReference<>(activity);
        this.retweetId = retweetId;
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
                    tweet = twitter.showTweet(tweetId);
                    //tweet = mTwitter.getStatus(tweetId);
                    publishProgress(tweet);
                    if (db.containStatus(tweetId)) {
                        // update tweet if there is a database entry
                        db.updateStatus(tweet);
                    }
                    break;

                case DELETE:
                    twitter.deleteTweet(tweetId);
                    db.removeStatus(tweetId);
                    // removing retweet reference to this tweet
                    if (retweetId > 0)
                        db.removeStatus(retweetId);
                    break;

                case RETWEET:
                    tweet = twitter.retweetTweet(tweetId);
                    publishProgress(tweet);
                    db.updateStatus(tweet);
                    break;

                case UNRETWEET:
                    tweet = twitter.unretweetTweet(tweetId);
                    publishProgress(tweet);
                    db.updateStatus(tweet);
                    // removing retweet reference to this tweet
                    if (tweet.getMyRetweetId() > 0)
                        db.removeStatus(retweetId);
                    break;

                case FAVORITE:
                    tweet = twitter.favoriteTweet(tweetId);
                    publishProgress(tweet);
                    db.storeFavorite(tweet);
                    break;

                case UNFAVORITE:
                    tweet = twitter.unfavoriteTweet(tweetId);
                    publishProgress(tweet);
                    db.removeFavorite(tweet);
                    break;
            }
            return action[0];
        } catch (TwitterException twException) {
            this.twException = twException;
            if (twException.getErrorType() == ErrorHandler.TwitterError.RESOURCE_NOT_FOUND) {
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