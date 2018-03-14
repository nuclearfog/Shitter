package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.UserRecycler;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.SearchPage;

import java.lang.ref.WeakReference;

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineRecycler tlRc;
    private UserRecycler uAdp;
    private RecyclerView tweetSearch, userSearch;
    private TwitterEngine mTwitter;
    private WeakReference<SearchPage> ui;
    private int background, font_color;

    public TwitterSearch(Context context) {
        ui = new WeakReference<>((SearchPage)context);
        tweetSearch = (RecyclerView) ui.get().findViewById(R.id.tweet_result);
        userSearch  = (RecyclerView) ui.get().findViewById(R.id.user_result);
        mTwitter = TwitterEngine.getInstance(context);
        ColorPreferences mcolor = ColorPreferences.getInstance(context);
        background = mcolor.getColor(ColorPreferences.BACKGROUND);
        font_color = mcolor.getColor(ColorPreferences.FONT_COLOR);
    }

    @Override
    protected Void doInBackground(String... search) {
        String strSearch = search[0];
        long id = 1L;
        try {
            tlRc = (TimelineRecycler) tweetSearch.getAdapter();
            if(tlRc != null) {
                id = tlRc.getItemId(0);
                tlRc.getData().insert(mTwitter.searchTweets(strSearch,id),false);
            } else {
                tlRc = new TimelineRecycler(new TweetDatabase(mTwitter.searchTweets(strSearch,id),ui.get()),ui.get());
                tlRc.setColor(background,font_color);
            }
            uAdp = new UserRecycler(new UserDatabase(ui.get(), mTwitter.searchUsers(strSearch)),ui.get());
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {

        SearchPage connect = ui.get();
        if(connect == null)
            return;

        SwipeRefreshLayout tweetReload = (SwipeRefreshLayout)connect.findViewById(R.id.searchtweets);
        ProgressBar circleLoad = (ProgressBar)connect.findViewById(R.id.search_progress);

        circleLoad.setVisibility(View.INVISIBLE);
        if(tweetSearch.getAdapter() == null) {
            tweetSearch.setAdapter(tlRc);
        } else {
            tlRc.notifyDataSetChanged();
        }
        userSearch.setAdapter(uAdp);
        tweetReload.setRefreshing(false);
    }
}