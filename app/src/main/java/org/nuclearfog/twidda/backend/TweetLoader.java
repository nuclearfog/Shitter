package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.Tweet;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragment.TweetFragment;

import java.lang.ref.WeakReference;
import java.util.List;

import static org.nuclearfog.twidda.fragment.TweetFragment.CLEAR_LIST;


/**
 * Background task to download a list of tweets from different sources
 *
 * @author nuclearfog
 * @see TweetFragment
 */
public class TweetLoader extends AsyncTask<Long, Void, List<Tweet>> {

    /**
     * Type of tweet list
     */
    public enum ListType {
        /**
         * tweets from home timeline
         */
        TL_HOME,
        /**
         * tweets from the mention timeline
         */
        TL_MENT,
        /**
         * tweets of an user
         */
        USR_TWEETS,
        /**
         * favorite tweets of an user
         */
        USR_FAVORS,
        /**
         * tweet replies to a tweet
         */
        REPLIES,
        /**
         * tweet replies from database
         */
        DB_ANS,
        /**
         * tweets from twitter search
         */
        TWEET_SEARCH,
        /**
         * tweets from an userlist
         */
        USERLIST,
        NONE
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<TweetFragment> callback;
    private final TwitterEngine mTwitter;
    private final AppDatabase db;

    private final ListType listType;
    private final String search;
    private final long id;
    private int pos;

    /**
     * @param callback callback to update tweet data
     * @param listType type of tweet list to load
     * @param id       ID, depending on what list type should be loaded
     * @param search   search string if any
     * @param pos      index of the list where tweets should be inserted
     */
    public TweetLoader(TweetFragment callback, ListType listType, long id, String search, int pos) {
        super();
        this.callback = new WeakReference<>(callback);
        db = new AppDatabase(callback.getContext());
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.listType = listType;
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
            switch (listType) {
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
                            tweets = db.getUserFavorites(id);
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

                case REPLIES:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getAnswers(id);
                        if (tweets.isEmpty()) {
                            tweets = mTwitter.getReplies(search, id, sinceId, maxId);
                            if (!tweets.isEmpty() && db.containStatus(id)) {
                                db.storeReplies(tweets);
                            }
                        }
                    } else if (sinceId > 0) {
                        tweets = mTwitter.getReplies(search, id, sinceId, maxId);
                        if (!tweets.isEmpty() && db.containStatus(id)) {
                            db.storeReplies(tweets);
                        }
                    } else if (maxId > 1) {
                        tweets = mTwitter.getReplies(search, id, sinceId, maxId);
                    }
                    break;

                case TWEET_SEARCH:
                    tweets = mTwitter.searchTweets(search, sinceId, maxId);
                    break;

                case USERLIST:
                    tweets = mTwitter.getListTweets(id, sinceId, maxId);
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
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