package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ListView;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.window.TwitterSearch;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

public class Search extends AsyncTask<String, Void, Boolean> {
    private TimelineAdapter tlAdp;
    private Context context;
    private Twitter twitter;
    private ListView tl;
    private int load;

    public Search(Context context) {
        this.context=context;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        load = settings.getInt("preload", 10);
    }

    @Override
    protected void onPreExecute() {
        tl = (ListView) ((TwitterSearch)context).findViewById(R.id.search_result);
        twitter = TwitterResource.getInstance(context).getTwitter();
    }

    @Override
    protected Boolean doInBackground(String... search) {
        String get = search[1];
        Query q = new Query();
        q.setQuery(get+" +exclude:retweets");
        q.setCount(load);
        try {
            switch(search[0]) {
                case("tweet"):
                    QueryResult result = twitter.search(q);
                    TweetDatabase searchdb = new TweetDatabase(result.getTweets(),context);
                    tlAdp = new TimelineAdapter(context, searchdb);
                    break;
                case("user"):
                    break;
            }
        } catch(Exception err){err.printStackTrace();}
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        tl.setAdapter(tlAdp);
    }
}