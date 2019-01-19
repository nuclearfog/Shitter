package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.window.SearchPage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class TwitterSearch extends AsyncTask<String, Integer, Void> {

    private final int TWEET = 0;
    private final int USER = 1;

    private TimelineAdapter searchAdapter;
    private UserAdapter userAdapter;
    private List<Tweet> tweets;
    private List<TwitterUser> users;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private WeakReference<SearchPage> ui;

    public TwitterSearch(SearchPage context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);

        tweets = new ArrayList<>();
        users = new ArrayList<>();

        RecyclerView tweetSearch = context.findViewById(R.id.tweet_result);
        RecyclerView userSearch = context.findViewById(R.id.user_result);
        searchAdapter = (TimelineAdapter) tweetSearch.getAdapter();
        userAdapter = (UserAdapter) userSearch.getAdapter();
    }


    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            if (searchAdapter.getItemCount() > 0)
                id = searchAdapter.getItemId(0);
            tweets = mTwitter.searchTweets(strSearch, id);
            publishProgress(TWEET);

            if (userAdapter.getItemCount() == 0) {
                users = mTwitter.searchUsers(strSearch);
                publishProgress(USER);
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            Log.e("Twitter Search", err.getMessage());
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Integer... mode) {
        if (ui.get() == null) return;

        switch (mode[0]) {
            case TWEET:
                searchAdapter.setData(tweets);
                searchAdapter.notifyDataSetChanged();
                SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
                tweetReload.setRefreshing(false);
                break;

            case USER:
                userAdapter.setData(users);
                userAdapter.notifyDataSetChanged();
                break;
        }
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        tweetReload.setRefreshing(false);

        if (err != null) {
            ErrorHandling.printError(ui.get(), err);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        tweetReload.setRefreshing(false);
    }
}