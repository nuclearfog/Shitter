package org.nuclearfog.twidda.Engine;

import org.nuclearfog.twidda.DataBase.TrendDatabase;
import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.ViewAdapter.TrendsAdapter;

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
    private ListView list;
    private TimelineAdapter timelineAdapter;
    private TrendsAdapter trendsAdapter;
    private SwipeRefreshLayout refresh;

    public TwitterEngine(Context context, ListView list) {
        this.context=context;
        this.list = list;
        twitterStore = TwitterStore.getInstance(context);
        twitterStore.init();
    }

    public void setRefresh(SwipeRefreshLayout refresh) {
        this.refresh=refresh;
    }

    /**
     * @param args modes
     */
    @Override
    protected Void doInBackground(Integer... args) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        Twitter twitter = twitterStore.getTwitter();

        try {
            switch(args[0]) {
                case (0): // Home Timeline
                    TweetDatabase mTweets = new TweetDatabase(twitter.getHomeTimeline(), context,TweetDatabase.HOME_TL);
                    timelineAdapter = new TimelineAdapter(context,R.layout.tweet,mTweets);

                break;
                case(1):  // Trends
                    TrendDatabase trend = new TrendDatabase(twitter.getPlaceTrends(23424829),context); //Germany by default
                    trendsAdapter = new TrendsAdapter(context,R.layout.tweet,trend);

                    break;
                case(2):  // Mentions
                    // TODO
                    break;
                case(3): // Home Tweets
                    TweetDatabase hTweets = new TweetDatabase(twitter.getUserTimeline(), context,TweetDatabase.USER_TL);
                    timelineAdapter = new TimelineAdapter(context,R.layout.tweet,hTweets);
                    break;

            }
        } catch (TwitterException e) {
            Toast.makeText(context, ERR_MSG, Toast.LENGTH_SHORT).show();
        } catch (Exception e){ e.printStackTrace(); }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        new Thread() {
            @Override
            public void run(){
                if(timelineAdapter != null) {
                    list.setAdapter(timelineAdapter);
                    timelineAdapter.notifyDataSetChanged();
                }
                else if(trendsAdapter != null) {
                    list.setAdapter(trendsAdapter);
                    trendsAdapter.notifyDataSetChanged();
                }
                if(refresh != null)
                    refresh.setRefreshing(false);
                //list.setTextFilterEnabled(true);
            }
        }.run();
    }
}