package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineAdapter searchAdapter;
    private UserAdapter userAdapter;
    private List<Tweet> tweets;
    private List<TwitterUser> users;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private WeakReference<SearchPage> ui;

    public TwitterSearch(@NonNull SearchPage context) {
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
    protected void onPreExecute() {
        if (ui.get() == null) return;

        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        tweetReload.setRefreshing(true);
    }


    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            if (searchAdapter.getItemCount() > 0)
                id = searchAdapter.getItemId(0);
            tweets = mTwitter.searchTweets(strSearch, id);

            if (userAdapter.getItemCount() == 0) {
                users = mTwitter.searchUsers(strSearch);
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            Log.e("Twitter Search", err.getMessage());
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() == null) return;

        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        tweetReload.setRefreshing(false);

        if(!tweets.isEmpty()) {
            searchAdapter.setData(tweets);
            searchAdapter.notifyDataSetChanged();
        }
        if(!users.isEmpty()) {
            userAdapter.setData(users);
            userAdapter.notifyDataSetChanged();
        }
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