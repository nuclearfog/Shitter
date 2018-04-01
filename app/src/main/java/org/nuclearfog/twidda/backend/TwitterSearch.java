package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.UserRecycler;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.backend.listitems.*;

import java.lang.ref.WeakReference;
import java.util.List;

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineRecycler tlRc;
    private UserRecycler uAdp;
    private RecyclerView tweetSearch, userSearch;
    private TwitterEngine mTwitter;
    private WeakReference<SearchPage> ui;
    private int highlight, font_color;
    private String error;
    boolean imageload;

    public TwitterSearch(Context context) {
        ui = new WeakReference<>((SearchPage)context);
        tweetSearch = (RecyclerView) ui.get().findViewById(R.id.tweet_result);
        userSearch  = (RecyclerView) ui.get().findViewById(R.id.user_result);
        mTwitter = TwitterEngine.getInstance(context);

        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        font_color = settings.getInt("font_color", 0xffffffff);
        highlight = settings.getInt("highlight_color", 0xffff00ff);
        imageload = settings.getBoolean("image_load",true);
    }

    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            tlRc = (TimelineRecycler) tweetSearch.getAdapter();
            uAdp = (UserRecycler) userSearch.getAdapter();

            if(tlRc != null && tlRc.getItemCount() > 0) {
                id = tlRc.getItemId(0);
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                tweets.addAll(tlRc.getData());
                tlRc = new TimelineRecycler(tweets,ui.get());
            } else {
                List<Tweet> tweets = mTwitter.searchTweets(strSearch,id);
                tlRc = new TimelineRecycler(tweets,ui.get());
            }
            if(uAdp == null ||uAdp.getItemCount() == 0) {
                List<TwitterUser> user = mTwitter.searchUsers(strSearch);
                uAdp = new UserRecycler(user, ui.get());
            }

            tlRc.setColor(highlight,font_color);
            tlRc.toggleImage(imageload);
            uAdp.toggleImage(imageload);

        } catch(Exception err) {
            error = err.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        SearchPage connect = ui.get();
        if(connect == null)
            return;
        if(error != null)
            Toast.makeText(connect, "Fehler beim Laden: "+error, Toast.LENGTH_LONG).show();

        SwipeRefreshLayout tweetReload = (SwipeRefreshLayout)connect.findViewById(R.id.searchtweets);
        ProgressBar circleLoad = (ProgressBar)connect.findViewById(R.id.search_progress);
        circleLoad.setVisibility(View.INVISIBLE);
        tweetSearch.setAdapter(tlRc);
        userSearch.setAdapter(uAdp);
        tweetReload.setRefreshing(false);
    }
}