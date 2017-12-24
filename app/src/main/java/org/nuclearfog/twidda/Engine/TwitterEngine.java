package org.nuclearfog.twidda.Engine;

import org.nuclearfog.twidda.DataBase.TrendDatabase;
import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.ViewAdapter.TrendsAdapter;
import org.nuclearfog.twidda.Window.Profile;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.os.AsyncTask;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterEngine extends AsyncTask<Long, Void, Void>
{
    private final String ERR_MSG = "Fehler bei der Verbindung";
    private TwitterStore twitterStore;
    private Context context;
    private ListView list;
    private TimelineAdapter timelineAdapter;
    private TrendsAdapter trendsAdapter;
    private SwipeRefreshLayout refresh;

    public TwitterEngine(Context context) {
        this.context=context;
        twitterStore = TwitterStore.getInstance(context);
        twitterStore.init();
    }

    @Override
    protected void onPreExecute() {
        refresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.refresh);
        list = (ListView)((MainActivity)context).findViewById(R.id.list);
    }

    /**
     * @param args [0] Executing Mode: (0)HomeTL, (1)Trend, (2)Mention
     *
     */
    @Override
    protected Void doInBackground(Long... args) {
        Twitter twitter = twitterStore.getTwitter();
        try {
            if(args[0]==0) {
                TweetDatabase mTweets = new TweetDatabase(twitter.getHomeTimeline(), context,TweetDatabase.HOME_TL);
                timelineAdapter = new TimelineAdapter(context,R.layout.tweet,mTweets);
            }
            else if(args[0]==1) {
                TrendDatabase trend = new TrendDatabase(twitter.getPlaceTrends(23424829),context); //Germany by default
                trendsAdapter = new TrendsAdapter(context,R.layout.tweet,trend);
            }
            else if(args[0]==2) { //TODO
            }
        } catch (TwitterException e) {
            Toast.makeText(context, ERR_MSG, Toast.LENGTH_SHORT).show();
        } catch (Exception e){ e.printStackTrace(); }
        return null;
    }

    /**
     * Refresh List in a new Thread
     */
    @Override
    protected void onPostExecute(Void v) {
        if(timelineAdapter != null) {
            list.setAdapter(timelineAdapter);
        }
        else if(trendsAdapter != null) {
            list.setAdapter(trendsAdapter);
        }
        if(refresh != null)
            refresh.setRefreshing(false);
    }
}