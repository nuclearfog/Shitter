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

import static org.nuclearfog.twidda.fragment.TweetFragment.CLEAR_LIST;


/**
 * Background task to download a list of tweets from different sources
 *
 * @see TweetFragment
 */
public class TweetListLoader extends AsyncTask<Long, Void, List<Tweet>> {

    public enum Action {
        TL_HOME,
        TL_MENT,
        USR_TWEETS,
        USR_FAVORS,
        TWEET_ANS,
        DB_ANS,
        TWEET_SEARCH,
        LIST,
        NONE
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<TweetFragment> callback;
    private final TwitterEngine mTwitter;
    private final AppDatabase db;

    private final Action action;
    private final String search;
    private final long id;
    private int pos;


    public TweetListLoader(TweetFragment callback, Action action, long id, String search, int pos) {
        super();
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.action = action;
        this.search = search;
        this.id = id;
        this.pos = pos;
    }


    @Override
    protected List<Tweet> doInBackground(Long[] param) {
        List<Tweet> tweets = null;
        long sinceId = param[0];
        long maxId = param[1];
        try {
            switch (action) {
                case TL_HOME:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getHomeTimeline();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getHome(sinceId, maxId);
                            db.storeHomeTimeline(tweets);
                        }
                    } else if (sinceId > 0) {
                        tweets = mTwitter.getHome(sinceId, maxId);
                        db.storeHomeTimeline(tweets);
                    } else if (maxId > 1) {
                        tweets = mTwitter.getHome(sinceId, maxId);
                    }
                    break;

                case TL_MENT:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getMentions();
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getMention(sinceId, maxId);
                            db.storeMentions(tweets);
                        }
                    } else if (sinceId > 0) {
                        tweets = mTwitter.getMention(sinceId, maxId);
                        db.storeMentions(tweets);
                    } else if (maxId > 1) {
                        tweets = mTwitter.getMention(sinceId, maxId);
                    }
                    break;

                case USR_TWEETS:
                    if (id > 0) {
                        if (sinceId == 0 && maxId == 0) {
                            tweets = db.getUserTweets(id);
                            if (tweets.isEmpty()) {
                                tweets = mTwitter.getUserTweets(id, 0, maxId);
                                db.storeUserTweets(tweets);
                            }
                        } else if (sinceId > 0) {
                            tweets = mTwitter.getUserTweets(id, sinceId, maxId);
                            db.storeUserTweets(tweets);
                        } else if (maxId > 1) {
                            tweets = mTwitter.getUserTweets(id, sinceId, maxId);
                        }
                    } else if (search != null) {
                        tweets = mTwitter.getUserTweets(search, sinceId, maxId);
                    }
                    break;

                case USR_FAVORS:
                    if (id > 0) {
                        if (sinceId == 0 && maxId == 0) {
                            tweets = db.getUserFavs(id);
                            if (tweets.isEmpty()) {
                                tweets = mTwitter.getUserFavs(id, 0, maxId);
                                db.storeUserFavs(tweets, id);
                            }
                        } else if (sinceId > 0) {
                            tweets = mTwitter.getUserFavs(id, 0, maxId);
                            db.storeUserFavs(tweets, id);
                            pos = CLEAR_LIST; // set flag to clear previous data
                        } else if (maxId > 1) {
                            tweets = mTwitter.getUserFavs(id, sinceId, maxId);
                        }
                    } else if (search != null) {
                        tweets = mTwitter.getUserFavs(search, sinceId, maxId);
                    }
                    break;

                case DB_ANS:
                    tweets = db.getAnswers(id);
                    break;

                case TWEET_ANS:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getAnswers(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getAnswers(search, id, sinceId, maxId);
                            if (!tweets.isEmpty() && db.containStatus(id)) {
                                db.storeReplies(tweets);
                            }
                        }
                    } else if (sinceId > 0) {
                        tweets = mTwitter.getAnswers(search, id, sinceId, maxId);
                        if (!tweets.isEmpty() && db.containStatus(id)) {
                            db.storeReplies(tweets);
                        }
                    } else if (maxId > 1) {
                        tweets = mTwitter.getAnswers(search, id, sinceId, maxId);
                    }
                    break;

                case TWEET_SEARCH:
                    tweets = mTwitter.searchTweets(search, sinceId, maxId);
                    break;

                case LIST:
                    tweets = mTwitter.getListTweets(id, sinceId, maxId);
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
            if (tweets != null) {
                callback.get().setData(tweets, pos);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}