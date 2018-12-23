package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

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

public class TwitterSearch extends AsyncTask<String, Integer, Boolean> {

    private final int TWEET = 0;
    private final int USER = 1;

    private TimelineAdapter searchAdapter;
    private UserAdapter userAdapter;
    private List<Tweet> tweets;
    private List<TwitterUser> users;
    private TwitterEngine mTwitter;
    private WeakReference<SearchPage> ui;
    private String errMsg = "E Twitter search: ";
    private int returnCode = 0;

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
    protected Boolean doInBackground(String... search) {
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
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 420)
                errMsg += err.getMessage();
            return false;
        } catch (Exception err) {
            errMsg += err.getMessage();
            Log.e("Twitter Search", errMsg);
            return false;
        }
        return true;
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
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        if (!success) {
            SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
            tweetReload.setRefreshing(false);

            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    break;
                default:
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCancelled(Boolean b) {
        if (ui.get() == null) return;

        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        tweetReload.setRefreshing(false);
    }
}