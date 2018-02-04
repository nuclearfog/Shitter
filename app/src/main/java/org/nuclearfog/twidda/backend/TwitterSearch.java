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
import org.nuclearfog.twidda.window.SearchWindow;

public class TwitterSearch extends AsyncTask<String, Void, String> {

    public static final String TWEETS = "tweets";
    public static final String USERS = "users";

    private TimelineAdapter tlAdp;
    private UserAdapter uAdp;
    private SwipeRefreshLayout tweetReload, userReload;
    private ListView tweetSearch, userSearch;
    private ProgressBar circleLoad;
    private Context context;
    private TwitterEngine mTwitter;

    public TwitterSearch(Context context) {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        tweetSearch = (ListView) ((SearchWindow)context).findViewById(R.id.tweet_result);
        userSearch  = (ListView) ((SearchWindow)context).findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) ((SearchWindow)context).findViewById(R.id.searchtweets);
        userReload  = (SwipeRefreshLayout) ((SearchWindow)context).findViewById(R.id.searchusers);
        circleLoad  = (ProgressBar) ((SearchWindow)context).findViewById(R.id.search_progress);
        mTwitter = TwitterEngine.getInstance(context);
    }

    @Override
    protected String doInBackground(String... search) {
        String mode = search[0];
        String get = search[1];
        long id = 1L;
        try {
            switch(mode) {
                case(TWEETS):
                    tlAdp = (TimelineAdapter) tweetSearch.getAdapter();
                    if(tlAdp != null) {
                        id = tlAdp.getItemId(0);
                        tlAdp.getData().addHot(mTwitter.searchTweets(get,id));
                    } else {
                        tlAdp = new TimelineAdapter(context, new TweetDatabase(mTwitter.searchTweets(get,id),context));
                    }
                    return TWEETS;
                case(USERS):
                    uAdp = new UserAdapter(context, new UserDatabase(context, mTwitter.searchUsers(get)));
                    return USERS;
            }
        } catch(Exception err){err.printStackTrace();}
        return "";
    }

    @Override
    protected void onPostExecute(String mode) {
        circleLoad.setVisibility(View.INVISIBLE);
        switch(mode) {
            case(TWEETS):
                if(tweetSearch.getAdapter() == null)
                    tweetSearch.setAdapter(tlAdp);
                else
                    tlAdp.notifyDataSetChanged();
                tweetReload.setRefreshing(false);
                break;
            case(USERS):
                userSearch.setAdapter(uAdp);
                userReload.setRefreshing(false);
        }
    }
}