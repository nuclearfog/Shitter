package org.nuclearfog.twidda.backend;

import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TrendAdapter;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.os.AsyncTask;

import twitter4j.TwitterException;

public class MainPage extends AsyncTask<Integer, Void, Boolean> {

    private TwitterEngine mTwitter;
    private Context context;
    private SwipeRefreshLayout timelineRefresh, trendRefresh, mentionRefresh;
    private ListView timelineList, trendList, mentionList;
    private TimelineAdapter timelineAdapter, mentionAdapter;
    private TrendAdapter trendsAdapter;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        this.context = context;
        mTwitter = TwitterEngine.getInstance(context);
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
        try {
            if(mode == 0) {
                TweetDatabase mTweets = new TweetDatabase(mTwitter.getHome(page), context,TweetDatabase.HOME_TL,0);
                timelineAdapter = new TimelineAdapter(context,mTweets);
            }
            else if(mode == 1) {
                trendsAdapter = new TrendAdapter(context, new TrendDatabase(mTwitter.getTrends(),context));
            }
            else if(mode == 2) {
                TweetDatabase mention = new TweetDatabase(mTwitter.getMention(page), context,TweetDatabase.GET_MENT,0);
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