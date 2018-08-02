package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.UserRecycler;
import org.nuclearfog.twidda.window.SearchPage;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineRecycler searchAdapter;
    private UserRecycler userAdapter;
    private TwitterEngine mTwitter;
    private ErrorLog errorLog;
    private WeakReference<SearchPage> ui;
    private int highlight, font_color;
    private boolean imageLoad;
    private int retryAfter = 0;

    public TwitterSearch(Context context) {
        ui = new WeakReference<>((SearchPage)context);
        mTwitter = TwitterEngine.getInstance(context);
        errorLog = new ErrorLog(context);

        GlobalSettings settings = GlobalSettings.getInstance(context);
        font_color = settings.getFontColor();
        highlight = settings.getHighlightColor();
        imageLoad = settings.loadImages();

        RecyclerView tweetSearch = ui.get().findViewById(R.id.tweet_result);
        RecyclerView userSearch = ui.get().findViewById(R.id.user_result);
        searchAdapter = (TimelineRecycler) tweetSearch.getAdapter();
        userAdapter = (UserRecycler) userSearch.getAdapter();

        if(searchAdapter == null) {
            searchAdapter = new TimelineRecycler(ui.get());
            tweetSearch.setAdapter(searchAdapter);
        }
        if(userAdapter == null) {
            userAdapter = new UserRecycler(ui.get());
            userSearch.setAdapter(userAdapter);
        }
    }


    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            if(searchAdapter.getItemCount() > 0) {
                id = searchAdapter.getItemId(0);
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                tweets.addAll(searchAdapter.getData());
                searchAdapter.setData(tweets);
            } else {
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                searchAdapter.setData(tweets);
            }
            if(userAdapter.getItemCount() == 0) {
                List<TwitterUser> user = mTwitter.searchUsers(strSearch);
                userAdapter.setData(user);
            }

            searchAdapter.setColor(highlight,font_color);
            searchAdapter.toggleImage(imageLoad);
            userAdapter.toggleImage(imageLoad);

        } catch (TwitterException err) {
            int errCode = err.getErrorCode();
            if(errCode == 420) {
                retryAfter = err.getRetryAfter();
            } else {
                String errorMessage = "E: " + err.getErrorMessage();
                errorLog.add(errorMessage);
            }
        } catch(Exception err) {
            String errorMessage = "E: Twitter search, " + err.getMessage();
            errorLog.add(errorMessage);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        SearchPage connect = ui.get();
        if(connect == null)
            return;
        if (retryAfter > 0) {
            Toast.makeText(connect, R.string.rate_limit_exceeded, Toast.LENGTH_LONG).show();
        }

        SwipeRefreshLayout tweetReload = connect.findViewById(R.id.searchtweets);
        View circleLoad = connect.findViewById(R.id.search_progress);
        circleLoad.setVisibility(View.INVISIBLE);
        searchAdapter.notifyDataSetChanged();
        userAdapter.notifyDataSetChanged();
        tweetReload.setRefreshing(false);
    }
}