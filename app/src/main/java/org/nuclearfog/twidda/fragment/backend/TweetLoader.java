package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class TweetLoader extends AsyncTask<Object, Void, Boolean> {

    public enum Mode {
        TL_HOME, DB_HOME,
        TL_MENT, DB_MENT,
        USR_TWEETS, DB_TWEETS,
        USR_FAVORS, DB_FAVORS,
        TWEET_ANS, DB_ANS,
        TWEET_SEARCH
    }

    private Mode mode;
    private WeakReference<View> ui;
    private TweetAdapter adapter;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private DatabaseAdapter db;
    private List<Tweet> tweets;
    private boolean loadAnswer;


    public TweetLoader(@NonNull View root, Mode mode) {
        ui = new WeakReference<>(root);
        mTwitter = TwitterEngine.getInstance(root.getContext());
        RecyclerView list = root.findViewById(R.id.fragment_list);
        adapter = (TweetAdapter) list.getAdapter();
        db = new DatabaseAdapter(root.getContext());
        if (mode == Mode.DB_ANS) {
            GlobalSettings settings = GlobalSettings.getInstance(root.getContext());
            loadAnswer = settings.getAnswerLoad();
        }
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
                if (getStatus() != Status.FINISHED)
                    reload.setRefreshing(true);
            }
        }, 500);
    }


    @Override
    protected Boolean doInBackground(Object[] param) {
        long sinceId = 1;
        try {
            switch (mode) {
                case DB_HOME:
                    tweets = db.getHomeTimeline();
                    if (!tweets.isEmpty())
                        break;

                case TL_HOME:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getHome(1, sinceId);
                    db.storeHomeTimeline(tweets);
                    tweets.addAll(adapter.getData());
                    break;

                case DB_MENT:
                    tweets = db.getMentions();
                    if (!tweets.isEmpty())
                        break;

                case TL_MENT:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getMention(1, sinceId);
                    db.storeMentions(tweets);
                    tweets.addAll(adapter.getData());
                    publishProgress();
                    break;

                case DB_TWEETS:
                    long tweetId = (long) param[0];
                    tweets = db.getUserTweets(tweetId);
                    if (!tweets.isEmpty())
                        break;

                case USR_TWEETS:
                    tweetId = (long) param[0];
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getUserTweets(tweetId, sinceId, 1);
                    db.storeUserTweets(tweets);
                    tweets.addAll(adapter.getData());
                    break;

                case DB_FAVORS:
                    tweetId = (long) param[0];
                    tweets = db.getUserFavs(tweetId);
                    if (!tweets.isEmpty())
                        break;

                case USR_FAVORS:
                    tweetId = (long) param[0];
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getUserFavs(tweetId, sinceId, 1);
                    db.storeUserFavs(tweets, tweetId);
                    tweets.addAll(adapter.getData());
                    break;

                case DB_ANS:
                    tweetId = (long) param[0];
                    tweets = db.getAnswers(tweetId);
                    if (!(tweets.isEmpty() && loadAnswer))
                        break;

                case TWEET_ANS:
                    String search = (String) param[1];
                    tweetId = (long) param[0];
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getAnswers(search, tweetId, sinceId);
                    if (!tweets.isEmpty() && db.containStatus(tweetId))
                        db.storeReplies(tweets);
                    tweets.addAll(adapter.getData());
                    break;

                case TWEET_SEARCH:
                    search = (String) param[0];
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.searchTweets(search, sinceId);
                    tweets.addAll(adapter.getData());
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            return false;
        } catch (Exception err) {
            if (err.getMessage() != null)
                Log.e("TweetLoader", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null)
            return;
        if (success) {
            adapter.setData(tweets);
            adapter.notifyDataSetChanged();
        } else {
            if (err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
        }
        SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.setRefreshing(false);
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null)
            return;
        SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.setRefreshing(false);
    }
}