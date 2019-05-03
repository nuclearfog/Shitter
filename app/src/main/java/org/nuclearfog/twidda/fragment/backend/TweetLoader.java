package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TweetAdapter;
import org.nuclearfog.twidda.backend.ErrorHandler;
import org.nuclearfog.twidda.backend.TwitterEngine;
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
    private GlobalSettings settings;
    private TwitterException err;
    private DatabaseAdapter db;
    private List<Tweet> tweets;


    public TweetLoader(@NonNull View root, Mode mode) {
        ui = new WeakReference<>(root);
        settings = GlobalSettings.getInstance(root.getContext());
        mTwitter = TwitterEngine.getInstance(root.getContext());
        RecyclerView list = root.findViewById(R.id.fragment_list);
        adapter = (TweetAdapter)list.getAdapter();
        db = new DatabaseAdapter(root.getContext());
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if(ui.get() == null)
            return;

        SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.setRefreshing(true);
    }


    @Override
    protected Boolean doInBackground(Object[] param) {
        long sinceId = 1;
        try {
            switch(mode) {
                case DB_HOME:
                    tweets = db.getHomeTimeline();
                    if(!tweets.isEmpty())
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
                    if(!tweets.isEmpty())
                        break;

                case TL_MENT:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getMention(1, sinceId);
                    tweets.addAll(adapter.getData());
                    db.storeMentions(tweets);
                    publishProgress();
                    break;


                case DB_TWEETS:
                    tweets = db.getUserTweets((long)param[0]);
                    if(!tweets.isEmpty())
                        break;

                case USR_TWEETS:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getUserTweets((long)param[0], sinceId, 1);
                    db.storeUserTweets(tweets);
                    tweets.addAll(adapter.getData());
                    break;

                case DB_FAVORS:
                    tweets = db.getUserFavs((long)param[0]);
                    if(!tweets.isEmpty())
                        break;

                case USR_FAVORS:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getUserFavs((long)param[0], sinceId, 1);
                    db.storeUserFavs(tweets, (long)param[0]);
                    tweets.addAll(adapter.getData());
                    break;

                case DB_ANS:
                    tweets = db.getAnswers((long)param[0]);
                    if(tweets.isEmpty() || !settings.getAnswerLoad())
                        break;

                case TWEET_ANS:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.getAnswers((String)param[1], (long)param[0], sinceId);
                    if (!tweets.isEmpty() && db.containStatus((long)param[0]))
                        db.storeReplies(tweets);
                    tweets.addAll(adapter.getData());
                    break;

                case TWEET_SEARCH:
                    if (adapter.getItemCount() > 0)
                        sinceId = adapter.getItemId(0);
                    tweets = mTwitter.searchTweets((String)param[0], sinceId);
                    break;
            }
        } catch(TwitterException err) {
            this.err = err;
            return false;
        }
        catch(Exception err) {
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if(ui.get() == null)
            return;

        if(success) {
            adapter.setData(tweets);
            adapter.notifyDataSetChanged();
        } else {
            if(err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
        }

        SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.setRefreshing(false);
    }


    @Override
    protected void onCancelled() {
        if(ui.get() == null)
            return;
        SwipeRefreshLayout reload = ui.get().findViewById(R.id.fragment_reload);
        reload.setRefreshing(false);
    }
}