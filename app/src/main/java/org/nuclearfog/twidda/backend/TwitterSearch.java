package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.SearchPage;

public class TwitterSearch extends AsyncTask<String, Void, Void> {

    private TimelineAdapter tlAdp;
    private UserAdapter uAdp;
    private SwipeRefreshLayout tweetReload;
    private ListView tweetSearch, userSearch;
    private ProgressBar circleLoad;
    private Context context;
    private TwitterEngine mTwitter;

    public TwitterSearch(Context context) {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        tweetSearch = (ListView) ((SearchPage)context).findViewById(R.id.tweet_result);
        userSearch  = (ListView) ((SearchPage)context).findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) ((SearchPage)context).findViewById(R.id.searchtweets);
        circleLoad  = (ProgressBar) ((SearchPage)context).findViewById(R.id.search_progress);
        mTwitter = TwitterEngine.getInstance(context);
    }

    @Override
    protected Void doInBackground(String... search) {
        String get = search[0];
        long id = 1L;
        try {
            tlAdp = (TimelineAdapter) tweetSearch.getAdapter();
            if(tlAdp != null) {
                id = tlAdp.getItemId(0);
                tlAdp.getData().insert(mTwitter.searchTweets(get,id),false);
            } else {
                tlAdp = new TimelineAdapter(context, new TweetDatabase(mTwitter.searchTweets(get,id),context));
            }
            uAdp = new UserAdapter(context, new UserDatabase(context, mTwitter.searchUsers(get)));
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        circleLoad.setVisibility(View.INVISIBLE);
        if(tweetSearch.getAdapter() == null)
            tweetSearch.setAdapter(tlAdp);
        else
            tlAdp.notifyDataSetChanged();
        userSearch.setAdapter(uAdp);
        tweetReload.setRefreshing(false);
    }
}