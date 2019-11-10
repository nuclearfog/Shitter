package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TweetListFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;


/**
 * Timeline loader Task
 */
public class TweetLoader extends AsyncTask<Object, Void, List<Tweet>> {

    public enum Mode {
        TL_HOME,
        TL_MENT,
        USR_TWEETS,
        USR_FAVORS,
        TWEET_ANS,
        DB_ANS,
        TWEET_SEARCH
    }

    @Nullable
    private TwitterException twException;
    private Mode mode;
    private WeakReference<TweetListFragment> ui;
    private TweetAdapter adapter;
    private TwitterEngine mTwitter;
    private AppDatabase db;


    public TweetLoader(TweetListFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null) {
            ui.get().setRefresh(true);
        }
    }


    @Override
    protected List<Tweet> doInBackground(Object[] param) {
        List<Tweet> tweets = null;
        long sinceId = 1;
        try {
            switch (mode) {
                case TL_HOME:
                    if (adapter.isEmpty()) {
                        tweets = db.getHomeTimeline();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getHome(1, sinceId);
                            db.storeHomeTimeline(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getHome(1, sinceId);
                        db.storeHomeTimeline(tweets);
                    }
                    break;

                case TL_MENT:
                    if (adapter.isEmpty()) {
                        tweets = db.getMentions();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getMention(1, sinceId);
                            db.storeMentions(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getMention(1, sinceId);
                        db.storeMentions(tweets);
                    }
                    break;

                case USR_TWEETS:
                    long tweetId = (long) param[0];
                    if (adapter.isEmpty()) {
                        tweets = db.getUserTweets(tweetId);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getUserTweets(tweetId, sinceId, 1);
                            db.storeUserTweets(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getUserTweets(tweetId, sinceId, 1);
                        db.storeUserTweets(tweets);
                    }
                    break;

                case USR_FAVORS:
                    tweetId = (long) param[0];
                    if (adapter.isEmpty()) {
                        tweets = db.getUserFavs(tweetId);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getUserFavs(tweetId, sinceId, 1);
                            db.storeUserFavs(tweets, tweetId);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getUserFavs(tweetId, sinceId, 1);
                        db.storeUserFavs(tweets, tweetId);
                    }
                    break;

                case DB_ANS:
                    tweetId = (long) param[0];
                    tweets = db.getAnswers(tweetId);
                    break;

                case TWEET_ANS:
                    tweetId = (long) param[0];
                    String search = (String) param[1];
                    if (adapter.isEmpty()) {
                        tweets = db.getAnswers(tweetId);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getAnswers(search, tweetId, sinceId);
                            if (!tweets.isEmpty() && db.containStatus(tweetId))
                                db.storeReplies(tweets);
                        }
                    } else {
                        sinceId = adapter.getItemId(0);
                        tweets = mTwitter.getAnswers(search, tweetId, sinceId);
                        if (!tweets.isEmpty() && db.containStatus(tweetId))
                            db.storeReplies(tweets);
                    }
                    break;

                case TWEET_SEARCH:
                    search = (String) param[0];
                    if (!adapter.isEmpty())
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.searchTweets(search, sinceId);
                    break;
            }
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return tweets;
    }


    @Override
    protected void onPostExecute(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null)
                adapter.addFirst(tweets);
            if (twException != null)
                ErrorHandler.printError(ui.get().getContext(), twException);
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null) {
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null)
                adapter.addFirst(tweets);
            ui.get().setRefresh(false);
        }
    }
}