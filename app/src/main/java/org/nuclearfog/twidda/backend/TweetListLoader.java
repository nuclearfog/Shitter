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
    private WeakReference<TweetFragment> ui;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private Action action;
    private long sinceId;


    public TweetListLoader(TweetFragment fragment, Action action) {
        ui = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        sinceId = fragment.getTopId();
        this.action = action;
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
        try {
            switch (action) {
                case TL_HOME:
                    int page = (int) param[0];
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
                    long id = (long) param[0];
                    page = (int) param[1];
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
                    break;

                case USR_FAVORS:
                    id = (long) param[0];
                    page = (int) param[1];
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
                    break;

                case DB_ANS:
                    id = (long) param[0];
                    tweets = db.getAnswers(id);
                    break;

                case TWEET_ANS:
                    id = (long) param[0];
                    String search = (String) param[1];
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
    protected void onPostExecute(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            ui.get().setRefresh(false);
            if (tweets != null) {
                if (action == Action.USR_FAVORS)
                    ui.get().add(tweets);
                else
                    ui.get().addTop(tweets);
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }
}