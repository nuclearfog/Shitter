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
    private ListView list, profileList;
    private TimelineAdapter timelineAdapter, homeTl;
    private TrendsAdapter trendsAdapter;
    private SwipeRefreshLayout refresh, refreshHome;

    public TwitterEngine(Context context) {
        this.context=context;
        twitterStore = TwitterStore.getInstance(context);
        twitterStore.init();
    }

    @Override
    protected void onPreExecute() {

        if(context.getClass() == MainActivity.class)
        {
            refresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.refresh);
            list = (ListView)((MainActivity)context).findViewById(R.id.list);
        }else {
        refreshHome = (SwipeRefreshLayout)((Profile)context).findViewById(R.id.refreshHome);
        profileList = (ListView)((Profile)context).findViewById(R.id.home_tl);
        }
    }


    /**
     * @param args [0] Executing Mode: (0)HomeTL, (1)Trend, (2)Mention, (3)UserTL
     *             [1] User ID
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
            else if(args[0]==3) {
                long userId = args[1];
                TweetDatabase hTweets = new TweetDatabase(twitter.getUserTimeline(userId), context,TweetDatabase.USER_TL);
                homeTl = new TimelineAdapter(context,R.layout.tweet,hTweets);
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
        new Thread() {
            @Override
            public void run(){
                if(timelineAdapter != null) {
                    list.setAdapter(timelineAdapter);
                }
                else if(trendsAdapter != null) {
                    list.setAdapter(trendsAdapter);
                }
                else if(homeTl != null) {
                    profileList.setAdapter(homeTl);
                }
                if(refresh != null)
                    refresh.setRefreshing(false);
                if(refreshHome!= null)
                    refreshHome.setRefreshing(false);
            }
        }.run();
    }
}