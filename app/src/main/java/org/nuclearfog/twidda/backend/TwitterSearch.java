package org.nuclearfog.twidda.backend;

import android.content.Context;
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

public class TwitterSearch extends AsyncTask<String, Void, Boolean> {

    private TimelineAdapter searchAdapter;
    private UserAdapter userAdapter;
    private TwitterEngine mTwitter;
    private WeakReference<SearchPage> ui;
    private String errorMessage = "E: Twitter search, ";
    private int returnCode = 0;

    public TwitterSearch(Context context) {
        ui = new WeakReference<>((SearchPage)context);
        mTwitter = TwitterEngine.getInstance(context);

        GlobalSettings settings = GlobalSettings.getInstance(context);
        int font_color = settings.getFontColor();
        int highlight = settings.getHighlightColor();
        boolean imageLoad = settings.loadImages();

        RecyclerView tweetSearch = ui.get().findViewById(R.id.tweet_result);
        RecyclerView userSearch = ui.get().findViewById(R.id.user_result);
        searchAdapter = (TimelineAdapter) tweetSearch.getAdapter();
        userAdapter = (UserAdapter) userSearch.getAdapter();

        if(searchAdapter == null) {
            searchAdapter = new TimelineAdapter(ui.get());
            tweetSearch.setAdapter(searchAdapter);
            searchAdapter.setColor(highlight, font_color);
            searchAdapter.toggleImage(imageLoad);
        }
        if(userAdapter == null) {
            userAdapter = new UserAdapter(ui.get());
            userSearch.setAdapter(userAdapter);
            userAdapter.toggleImage(imageLoad);
        }
    }


    @Override
    protected Boolean doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            if(searchAdapter.getItemCount() > 0) {
                id = searchAdapter.getItemId(0);
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                searchAdapter.addNew(tweets);
            } else {
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                searchAdapter.setData(tweets);
            }
            if(userAdapter.getItemCount() == 0) {
                List<TwitterUser> user = mTwitter.searchUsers(strSearch);
                userAdapter.setData(user);
            }
            return true;

        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 420) {
                errorMessage += err.getMessage();
            }
        } catch(Exception err) {
            Log.e("Twitter Search", err.getMessage());
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        SearchPage connect = ui.get();
        if(connect == null)
            return;
        if (!success) {
            if (returnCode == 420) {
                Toast.makeText(connect, R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
            } else if (returnCode > 0) {
                Toast.makeText(connect, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
        SwipeRefreshLayout tweetReload = connect.findViewById(R.id.searchtweets);
        searchAdapter.notifyDataSetChanged();
        userAdapter.notifyDataSetChanged();
        tweetReload.setRefreshing(false);
    }
}