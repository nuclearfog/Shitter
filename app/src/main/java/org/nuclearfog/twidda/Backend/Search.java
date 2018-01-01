package org.nuclearfog.twidda.Backend;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.Window.TwitterSearch;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

public class Search extends AsyncTask<String, Void, Boolean> {

    private TimelineAdapter tlAdp;
    private Context context;
    private Twitter twitter;
    private ListView tl;

    public Search(Context context) {
        this.context=context;
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
        q.setQuery(get);
        q.setCount(50);
        try {
            switch(search[0]){
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