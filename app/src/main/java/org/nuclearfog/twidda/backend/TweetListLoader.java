package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TweetFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static org.nuclearfog.twidda.fragment.TweetFragment.LIST_EMPTY;


/**
 * Background task to download a list of tweets from different sources
 *
 * @see TweetFragment
 */
public class TweetListLoader extends AsyncTask<Object, Void, List<Tweet>> {

    public enum Action {
        TL_HOME,
        TL_MENT,
        USR_TWEETS,
        USR_FAVORS,
        TWEET_ANS,
        DB_ANS,
        TWEET_SEARCH,
        LIST
    }

    @Nullable
    private EngineException twException;
    private WeakReference<TweetFragment> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private final Action action;
    private long sinceId;


    public TweetListLoader(TweetFragment callback, Action action) {
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        sinceId = callback.getTopId();
        this.action = action;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setRefresh(true);
        }
    }


    @Override
    protected List<Tweet> doInBackground(Object[] param) {
        List<Tweet> tweets = null;
        String search;
        int page;
        long id;

        try {
            switch (action) {
                case TL_HOME:
                    page = (int) param[0];
                    if (sinceId == LIST_EMPTY) {
                        tweets = db.getHomeTimeline();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getHome(page, sinceId);
                            db.storeHomeTimeline(tweets);
                        }
                    } else {
                        tweets = mTwitter.getHome(page, sinceId);
                        db.storeHomeTimeline(tweets);
                    }
                    break;

                case TL_MENT:
                    page = (int) param[0];
                    if (sinceId == LIST_EMPTY) {
                        tweets = db.getMentions();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getMention(page, sinceId);
                            db.storeMentions(tweets);
                        }
                    } else {
                        tweets = mTwitter.getMention(page, sinceId);
                        db.storeMentions(tweets);
                    }
                    break;

                case USR_TWEETS:
                    page = (int) param[1];
                    if (param[0] instanceof Long) { // search by user ID
                        id = (long) param[0];
                        if (sinceId == LIST_EMPTY) {
                            tweets = db.getUserTweets(id);
                            if (tweets.isEmpty()) {
                                tweets = mTwitter.getUserTweets(id, sinceId, page);
                                db.storeUserTweets(tweets);
                            }
                        } else {
                            tweets = mTwitter.getUserTweets(id, sinceId, page);
                            db.storeUserTweets(tweets);
                        }
                    } else if (param[0] instanceof String) { // search by username
                        search = (String) param[0];
                        tweets = mTwitter.getUserTweets(search, sinceId, page);
                    }
                    break;

                case USR_FAVORS:
                    page = (int) param[1];
                    if (param[0] instanceof Long) { // search by user ID
                        id = (long) param[0];
                        if (sinceId == LIST_EMPTY) {
                            tweets = db.getUserFavs(id);
                            if (tweets.isEmpty()) {
                                tweets = mTwitter.getUserFavs(id, page);
                                db.storeUserFavs(tweets, id);
                            }
                        } else {
                            tweets = mTwitter.getUserFavs(id, page);
                            db.storeUserFavs(tweets, id);
                        }
                    } else if (param[0] instanceof String) { // search by username
                        search = (String) param[0];
                        tweets = mTwitter.getUserFavs(search, page);
                    }
                    break;

                case DB_ANS:
                    id = (long) param[0];
                    tweets = db.getAnswers(id);
                    break;

                case TWEET_ANS:
                    id = (long) param[0];
                    search = (String) param[1];
                    if (sinceId == LIST_EMPTY) {
                        tweets = db.getAnswers(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getAnswers(search, id, sinceId);
                            if (!tweets.isEmpty() && db.containStatus(id))
                                db.storeReplies(tweets);
                        }
                    } else {
                        tweets = mTwitter.getAnswers(search, id, sinceId);
                        if (!tweets.isEmpty() && db.containStatus(id))
                            db.storeReplies(tweets);
                    }
                    break;

                case TWEET_SEARCH:
                    search = (String) param[0];
                    tweets = mTwitter.searchTweets(search, sinceId);
                    break;

                case LIST:
                    id = (long) param[0];
                    page = (int) param[1];
                    tweets = mTwitter.getListTweets(id, sinceId, page);
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return tweets;
    }


    @Override
    protected void onPostExecute(List<Tweet> tweets) {
        if (callback.get() != null) {
            callback.get().setRefresh(false);
            if (tweets != null) {
                if (action == Action.USR_FAVORS)
                    callback.get().add(tweets);
                else
                    callback.get().addTop(tweets);
            } else if (twException != null) {
                callback.get().onError(twException);
            }
        }
    }
}