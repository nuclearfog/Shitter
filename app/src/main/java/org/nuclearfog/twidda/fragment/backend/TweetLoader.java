package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

import static android.os.AsyncTask.Status.FINISHED;

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
    private Mode mode;
    private WeakReference<View> ui;
    private TweetAdapter adapter;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private DatabaseAdapter db;


    public TweetLoader(@NonNull View root, Mode mode) {
        ui = new WeakReference<>(root);
        mTwitter = TwitterEngine.getInstance(root.getContext());
        RecyclerView list = root.findViewById(R.id.fragment_list);
        adapter = (TweetAdapter) list.getAdapter();
        db = new DatabaseAdapter(root.getContext());
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() == null)
            return;
        final SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getStatus() != FINISHED)
                    reload.setRefreshing(true);
            }
        }, 500);
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
                        tweets.addAll(adapter.getData());
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
                        tweets.addAll(adapter.getData());
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
                        tweets.addAll(adapter.getData());
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
                        tweets.addAll(adapter.getData());
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
                        tweets.addAll(adapter.getData());
                    }
                    break;

                case TWEET_SEARCH:
                    search = (String) param[0];
                    if (!adapter.isEmpty())
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.searchTweets(search, sinceId);
                    if (!adapter.isEmpty())
                        tweets.addAll(adapter.getData());
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return tweets;
    }


    @Override
    protected void onPostExecute(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null) {
                adapter.setData(tweets);
                adapter.notifyDataSetChanged();
            } else {
                if (err != null)
                    ErrorHandler.printError(ui.get().getContext(), err);
            }
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null) {
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }


    @Override
    protected void onCancelled(@Nullable List<Tweet> tweets) {
        if (ui.get() != null) {
            if (tweets != null) {
                adapter.setData(tweets);
                adapter.notifyDataSetChanged();
            }
            SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
            reload.setRefreshing(false);
        }
    }
}