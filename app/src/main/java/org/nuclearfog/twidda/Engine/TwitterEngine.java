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

public class TwitterEngine extends AsyncTask<Integer, Void, Void>
{
    private final String ERR_MSG = "Fehler bei der Verbindung";
    private TwitterStore twitterStore;
    private Context context;

    private SwipeRefreshLayout timelineRefresh, trendRefresh, mentionRefresh;
    private ListView timelineList, trendList, mentionList;
    private TimelineAdapter timelineAdapter;
    private TrendsAdapter trendsAdapter;


    public TwitterEngine(Context context) {
        this.context=context;
        twitterStore = TwitterStore.getInstance(context);
        twitterStore.init();
    }

    @Override
    protected void onPreExecute() {
        // Timeline Tab
        timelineRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.timeline);
        timelineList = (ListView)((MainActivity)context).findViewById(R.id.tl_list);
        // Trend Tab
        trendRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.trends);
        trendList = (ListView)((MainActivity)context).findViewById(R.id.tr_list);
        // Mention Tab
        mentionRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.mention);
        mentionList = (ListView)((MainActivity)context).findViewById(R.id.m_list);
    }

    /**
     * @param args [0] Executing Mode: (0)HomeTL, (1)Trend, (2)Mention
     *
     */
    @Override
    protected Void doInBackground(Integer... args) {
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
        if(timelineAdapter != null)
            timelineList.setAdapter(timelineAdapter);
        else if(trendsAdapter != null)
            trendList.setAdapter(trendsAdapter);
        if(timelineRefresh.isRefreshing())
            timelineRefresh.setRefreshing(false);
        else if(mentionRefresh.isRefreshing())
            mentionRefresh.setRefreshing(false);
        else if(trendRefresh.isRefreshing())
            trendRefresh.setRefreshing(false);
    }
}