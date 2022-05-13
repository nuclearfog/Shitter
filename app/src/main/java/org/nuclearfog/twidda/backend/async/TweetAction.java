package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.ui.activities.TweetActivity;
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
public class TweetAction extends AsyncTask<Void, Tweet, Void> {

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
         * hide reply
         */
        HIDE,
        /**
         * unhide reply
         */
        UNHIDE,
        /**
         * delete own tweet
         */
        DELETE
    }

    @Nullable
    private TwitterException twException;
    private Twitter twitter;
    private WeakReference<TweetActivity> weakRef;
    private AppDatabase db;

    private Action action;
    private long tweetId, retweetId;


    /**
     * @param tweetId ID of the tweet
     */
    public TweetAction(TweetActivity activity, Action action, long tweetId, long retweetId) {
        super();
        db = new AppDatabase(activity);
        twitter = Twitter.get(activity);
        weakRef = new WeakReference<>(activity);

        this.action = action;
        this.retweetId = retweetId;
        this.tweetId = tweetId;
    }


    @Override
    protected Void doInBackground(Void... v) {
        try {
            switch (action) {
                case LD_DB:
                    Tweet tweet = db.getTweet(tweetId);
                    if (tweet != null) {
                        publishProgress(tweet);
                    }

                case LOAD:
                    tweet = twitter.showTweet(tweetId);
                    //tweet = mTwitter.getStatus(tweetId);
                    publishProgress(tweet);
                    if (db.containsTweet(tweetId)) {
                        // update tweet if there is a database entry
                        db.updateTweet(tweet);
                    }
                    break;

                case DELETE:
                    twitter.deleteTweet(tweetId);
                    db.removeTweet(tweetId);
                    // removing retweet reference to this tweet
                    if (retweetId > 0)
                        db.removeTweet(retweetId);
                    break;

                case RETWEET:
                    tweet = twitter.retweetTweet(tweetId);
                    publishProgress(tweet);
                    db.updateTweet(tweet);
                    break;

                case UNRETWEET:
                    tweet = twitter.unretweetTweet(tweetId);
                    publishProgress(tweet);
                    db.updateTweet(tweet);
                    // removing retweet reference to this tweet
                    if (retweetId > 0)
                        db.removeTweet(retweetId);
                    else
                        db.removeTweet(tweetId);
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

                case HIDE:
                    twitter.hideReply(tweetId, true);
                    db.hideReply(tweetId, true);
                    break;

                case UNHIDE:
                    twitter.hideReply(tweetId, false);
                    db.hideReply(tweetId, false);
                    break;
            }
        } catch (TwitterException twException) {
            this.twException = twException;
            if (twException.getErrorType() == ErrorHandler.TwitterError.RESOURCE_NOT_FOUND) {
                db.removeTweet(tweetId);
            }
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Tweet... tweets) {
        TweetActivity activity = weakRef.get();
        if (activity != null && tweets.length > 0 && tweets[0] != null) {
            activity.setTweet(tweets[0]);
        }
    }


    @Override
    protected void onPostExecute(Void v) {
        TweetActivity activity = weakRef.get();
        if (activity != null) {
            if (twException == null) {
                activity.OnSuccess(action, tweetId);
            } else {
                activity.onError(twException, tweetId);
            }
        }
    }
}