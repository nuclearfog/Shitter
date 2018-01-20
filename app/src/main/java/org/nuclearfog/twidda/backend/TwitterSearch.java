package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.SearchWindow;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

public class TwitterSearch extends AsyncTask<String, Void, String> {

    public static final String TWEETS = "tweets";
    public static final String USERS = "users";

    private TimelineAdapter tlAdp;
    private UserAdapter uAdp;
    private SwipeRefreshLayout tweetReload, userReload;
    private ListView tweetSearch, userSearch;
    private Context context;
    private Twitter twitter;
    private int load;

    public TwitterSearch(Context context) {
        this.context=context;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        load = settings.getInt("preload", 10) + 1;
    }

    @Override
    protected void onPreExecute() {
        tweetSearch = (ListView) ((SearchWindow)context).findViewById(R.id.tweet_result);
        userSearch  = (ListView) ((SearchWindow)context).findViewById(R.id.user_result);
        tweetReload = (SwipeRefreshLayout) ((SearchWindow)context).findViewById(R.id.searchtweets);
        userReload  = (SwipeRefreshLayout) ((SearchWindow)context).findViewById(R.id.searchusers);
        twitter = TwitterResource.getInstance(context).getTwitter();
    }

    @Override
    protected String doInBackground(String... search) {
        String mode = search[0];
        String get = search[1];
        try {
            switch(mode) {
                case(TWEETS):
                    Query q = new Query();
                    q.setQuery(get+" +exclude:retweets");
                    q.setCount(load);
                    QueryResult result = twitter.search(q);
                    TweetDatabase searchdb = new TweetDatabase(result.getTweets(),context);
                    tlAdp = new TimelineAdapter(context, searchdb);
                    return TWEETS;
                case(USERS):
                    UserDatabase userdb = new UserDatabase(context, twitter.searchUsers(get,-1));
                    uAdp = new UserAdapter(context, userdb);
                    return USERS;
            }
        } catch(Exception err){err.printStackTrace();}
        return "";
    }

    @Override
    protected void onPostExecute(String mode) {
        switch(mode) {
            case(TWEETS):
                tweetSearch.setAdapter(tlAdp);
                tweetReload.setRefreshing(false);
                break;
            case(USERS):
                userSearch.setAdapter(uAdp);
                userReload.setRefreshing(false);
        }
    }
}