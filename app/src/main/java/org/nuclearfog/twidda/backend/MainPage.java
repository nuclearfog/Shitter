package org.nuclearfog.twidda.backend;

import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TrendAdapter;

import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.os.AsyncTask;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MainPage extends AsyncTask<Integer, Void, Boolean>
{
    private TwitterResource twitterResource;
    private Context context;

    private SwipeRefreshLayout timelineRefresh, trendRefresh, mentionRefresh;
    private ListView timelineList, trendList, mentionList;
    private TimelineAdapter timelineAdapter, mentionAdapter;
    private TrendAdapter trendsAdapter;
    private SharedPreferences settings;
    private int load;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        this.context = context;
        twitterResource = TwitterResource.getInstance(context);
        twitterResource.init();// preload
        settings = context.getSharedPreferences("settings", 0);
        load = settings.getInt("preload", 10) + 1;
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
     * @return success
     */
    @Override
    protected Boolean doInBackground(Integer... args) {
        int mode = args[0];
        int page = args[1];
        Twitter twitter = twitterResource.getTwitter();
        Paging p = new Paging(page, load);
        try {
            if(mode == 0) {
                TweetDatabase mTweets = new TweetDatabase(twitter.getHomeTimeline(p), context,TweetDatabase.HOME_TL,0);
                timelineAdapter = new TimelineAdapter(context,mTweets);
            }
            else if(mode == 1) {
                int location = settings.getInt("woeid",23424829);
                TrendDatabase trend = new TrendDatabase(twitter.getPlaceTrends(location),context); //Germany by default
                trendsAdapter = new TrendAdapter(context,trend);
            }
            else if(mode == 2) {
                TweetDatabase mention = new TweetDatabase(twitter.getMentionsTimeline(), context,TweetDatabase.GET_MENT,0);
                mentionAdapter = new TimelineAdapter(context,mention);
            }
        } catch (TwitterException e) {
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if(success) {
            if(timelineAdapter != null)
                timelineList.setAdapter(timelineAdapter);
            else if(trendsAdapter != null)
                trendList.setAdapter(trendsAdapter);
            else if(mentionAdapter != null)
                mentionList.setAdapter(mentionAdapter);
        } else {
            Toast.makeText(context, context.getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
        }
        if(timelineRefresh.isRefreshing())
            timelineRefresh.setRefreshing(false);
        else if(mentionRefresh.isRefreshing())
            mentionRefresh.setRefreshing(false);
        else if(trendRefresh.isRefreshing())
            trendRefresh.setRefreshing(false);
    }
}