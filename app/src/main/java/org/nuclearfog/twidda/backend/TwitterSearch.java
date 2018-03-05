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

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineRecycler tlRc;
    private UserRecycler uAdp;
    private SwipeRefreshLayout tweetReload;
    private RecyclerView tweetSearch, userSearch;
    private ProgressBar circleLoad;
    private Context context;
    private TwitterEngine mTwitter;
    private int background, font_color;

    public TwitterSearch(Context context) {
        this.context=context;
        ColorPreferences mcolor = ColorPreferences.getInstance(context);
        background = mcolor.getColor(ColorPreferences.BACKGROUND);
        font_color = mcolor.getColor(ColorPreferences.FONT_COLOR);
    }

    @Override
    protected void onPreExecute() {
        tweetSearch = (RecyclerView) ((SearchPage)context).findViewById(R.id.tweet_result);
        userSearch  = (RecyclerView) ((SearchPage)context).findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) ((SearchPage)context).findViewById(R.id.searchtweets);
        circleLoad  = (ProgressBar) ((SearchPage)context).findViewById(R.id.search_progress);
        mTwitter = TwitterEngine.getInstance(context);
    }

    @Override
    protected Void doInBackground(String... search) {
        String get = search[0];
        long id = 1L;
        try {
            tlRc = (TimelineRecycler) tweetSearch.getAdapter();
            if(tlRc != null) {
                id = tlRc.getItemId(0);
                tlRc.getData().insert(mTwitter.searchTweets(get,id),false);
            } else {
                tlRc = new TimelineRecycler(new TweetDatabase(mTwitter.searchTweets(get,id),context),((SearchPage)context));
                tlRc.setColor(background,font_color);
            }
            uAdp = new UserRecycler(new UserDatabase(context, mTwitter.searchUsers(get)),((SearchPage)context));
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        circleLoad.setVisibility(View.INVISIBLE);
        if(tweetSearch.getAdapter() == null)
            tweetSearch.setAdapter(tlRc);
        else
            tlRc.notifyDataSetChanged();
        userSearch.setAdapter(uAdp);
        tweetReload.setRefreshing(false);
    }
}