package org.nuclearfog.twidda.backend;

import static org.nuclearfog.twidda.fragments.TweetFragment.CLEAR_LIST;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler.TwitterError;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.fragments.TweetFragment;
import org.nuclearfog.twidda.model.Tweet;

import java.lang.ref.WeakReference;
import java.util.List;

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
    private TwitterError twException;
    private WeakReference<TweetFragment> callback;
    private Twitter twitter;
    private AppDatabase db;

    private ListType listType;
    private String search;
    private long id;
    private int pos;

    /**
     * @param fragment callback to update tweet data
     * @param listType type of tweet list to load
     * @param id       ID, depending on what list type should be loaded
     * @param search   search string if any
     * @param pos      index of the list where tweets should be inserted
     */
    public TweetLoader(TweetFragment fragment, ListType listType, long id, String search, int pos) {
        super();
        this.callback = new WeakReference<>(fragment);
        db = new AppDatabase(fragment.getContext());
        twitter = Twitter.get(fragment.getContext());

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
                            tweets = twitter.getHomeTimeline(sinceId, maxId);
                            db.storeHomeTimeline(tweets);
                        }
                    } else if (sinceId > 0) {
                        tweets = twitter.getHomeTimeline(sinceId, maxId);
                        db.storeHomeTimeline(tweets);
                    } else if (maxId > 1) {
                        tweets = twitter.getHomeTimeline(sinceId, maxId);
                    }
                    break;

                case TL_MENT:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getMentions();
                        if (tweets.isEmpty()) {
                            tweets = twitter.getMentionTimeline(sinceId, maxId);
                            db.storeMentions(tweets);
                        }
                    } else if (sinceId > 0) {
                        tweets = twitter.getMentionTimeline(sinceId, maxId);
                        db.storeMentions(tweets);
                    } else if (maxId > 1) {
                        tweets = twitter.getMentionTimeline(sinceId, maxId);
                    }
                    break;

                case USR_TWEETS:
                    if (id > 0) {
                        if (sinceId == 0 && maxId == 0) {
                            tweets = db.getUserTweets(id);
                            if (tweets.isEmpty()) {
                                tweets = twitter.getUserTimeline(id, 0, maxId);
                                db.storeUserTweets(tweets);
                            }
                        } else if (sinceId > 0) {
                            tweets = twitter.getUserTimeline(id, sinceId, maxId);
                            db.storeUserTweets(tweets);
                        } else if (maxId > 1) {
                            tweets = twitter.getUserTimeline(id, sinceId, maxId);
                        }
                    } else if (search != null) {
                        tweets = twitter.getUserTimeline(search, sinceId, maxId);
                    }
                    break;

                case USR_FAVORS:
                    if (id > 0) {
                        if (sinceId == 0 && maxId == 0) {
                            tweets = db.getUserFavorites(id);
                            if (tweets.isEmpty()) {
                                tweets = twitter.getUserFavorits(id, 0, maxId);
                                db.storeUserFavs(tweets, id);
                            }
                        } else if (sinceId > 0) {
                            tweets = twitter.getUserFavorits(id, 0, maxId);
                            db.storeUserFavs(tweets, id);
                            pos = CLEAR_LIST; // set flag to clear previous data
                        } else if (maxId > 1) {
                            tweets = twitter.getUserFavorits(id, sinceId, maxId);
                        }
                    } else if (search != null) {
                        tweets = twitter.getUserFavorits(search, sinceId, maxId);
                    }
                    break;

                case DB_ANS:
                    tweets = db.getAnswers(id);
                    break;

                case REPLIES:
                    if (sinceId == 0 && maxId == 0) {
                        tweets = db.getAnswers(id);
                        if (tweets.isEmpty()) {
                            tweets = twitter.getTweetReplies(search, id, sinceId, maxId);
                            if (!tweets.isEmpty() && db.containStatus(id)) {
                                db.storeReplies(tweets);
                            }
                        }
                    } else if (sinceId > 0) {
                        tweets = twitter.getTweetReplies(search, id, sinceId, maxId);
                        if (!tweets.isEmpty() && db.containStatus(id)) {
                            db.storeReplies(tweets);
                        }
                    } else if (maxId > 1) {
                        tweets = twitter.getTweetReplies(search, id, sinceId, maxId);
                    }
                    break;

                case TWEET_SEARCH:
                    tweets = twitter.searchTweets(search, sinceId, maxId);
                    break;

                case USERLIST:
                    tweets = twitter.getUserlistTweets(id, sinceId, maxId);
                    break;
            }
        } catch (TwitterException e) {
            this.twException = e;
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