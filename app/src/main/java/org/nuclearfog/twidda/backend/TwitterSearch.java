package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.SearchPage;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineAdapter searchAdapter;
    private UserAdapter userAdapter;
    private TwitterEngine mTwitter;
    private WeakReference<SearchPage> ui;
    private String errorMessage = "E: Twitter search, ";
    private int returnCode = 0;

    public TwitterSearch(SearchPage context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);

        GlobalSettings settings = GlobalSettings.getInstance(context);
        int font_color = settings.getFontColor();
        int highlight = settings.getHighlightColor();
        boolean imageLoad = settings.loadImages();

        RecyclerView tweetSearch = context.findViewById(R.id.tweet_result);
        RecyclerView userSearch = context.findViewById(R.id.user_result);
        searchAdapter = (TimelineAdapter) tweetSearch.getAdapter();
        userAdapter = (UserAdapter) userSearch.getAdapter();

        if (searchAdapter == null) {
            searchAdapter = new TimelineAdapter(context);
            tweetSearch.setAdapter(searchAdapter);
            searchAdapter.setColor(highlight, font_color);
            searchAdapter.toggleImage(imageLoad);
        }
        if (userAdapter == null) {
            userAdapter = new UserAdapter(context);
            userSearch.setAdapter(userAdapter);
            userAdapter.toggleImage(imageLoad);
        }
    }


    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            if (searchAdapter.getItemCount() > 0) {
                id = searchAdapter.getItemId(0);
                List<Tweet> tweets = mTwitter.searchTweets(strSearch, id);
                searchAdapter.addNew(tweets);
            } else {
                List<Tweet> tweets = mTwitter.searchTweets(strSearch, id);
                searchAdapter.setData(tweets);
            }
            publishProgress();

            if (userAdapter.getItemCount() == 0) {
                List<TwitterUser> user = mTwitter.searchUsers(strSearch);
                userAdapter.setData(user);
                publishProgress();
            }
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 420) {
                errorMessage += err.getMessage();
            }
        } catch (Exception err) {
            Log.e("Twitter Search", err.getMessage());
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Void... v) {
        if (ui.get() == null) return;
        SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
        searchAdapter.notifyDataSetChanged();
        userAdapter.notifyDataSetChanged();
        tweetReload.setRefreshing(false);
    }


    @Override
    protected void onPostExecute(Void v) {
        if (ui.get() != null) {
            SwipeRefreshLayout tweetReload = ui.get().findViewById(R.id.searchtweets);
            tweetReload.setRefreshing(false);
            if (returnCode == 420)
                Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
            else if (returnCode > 0)
                Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }
}