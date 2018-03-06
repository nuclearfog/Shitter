package org.nuclearfog.twidda.backend;

import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TrendRecycler;

import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;
import android.content.Context;
import android.os.AsyncTask;

public class MainPage extends AsyncTask<Integer, Void, Integer> {

    public static final int HOME = 0;
    public static final int TRND = 1;
    public static final int MENT = 2;
    private static final int FAIL = -1;

    private TwitterEngine mTwitter;
    private Context context;
    private SwipeRefreshLayout timelineRefresh, trendRefresh, mentionRefresh;
    private RecyclerView timelineList, trendList, mentionList;
    private TimelineRecycler timelineAdapter, mentionAdapter;
    private TrendRecycler trendsAdapter;
    private int woeid;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        this.context = context;
        mTwitter = TwitterEngine.getInstance(context);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        woeid = settings.getInt("woeid",23424829); // Germany WOEID
    }

    @Override
    protected void onPreExecute() {
        // Timeline Tab
        timelineRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.timeline);
        timelineList = (RecyclerView)((MainActivity)context).findViewById(R.id.tl_list);
        // Trend Tab
        trendRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.trends);
        trendList = (RecyclerView)((MainActivity)context).findViewById(R.id.tr_list);
        // Mention Tab
        mentionRefresh = (SwipeRefreshLayout)((MainActivity)context).findViewById(R.id.mention);
        mentionList = (RecyclerView)((MainActivity)context).findViewById(R.id.m_list);
    }

    /**
     * @param args [0] Executing Mode: (0)HomeTL, (1)Trend, (2)Mention
     * @return success
     */
    @Override
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        int page = args[1];
        long id = 1L;
        try {
            switch (MODE) {
                case HOME:
                    timelineAdapter = (TimelineRecycler) timelineList.getAdapter();
                    if(timelineAdapter != null && timelineAdapter.getItemCount() > 0) {
                        id = timelineAdapter.getItemId(0);
                        timelineAdapter.getData().insert(mTwitter.getHome(page,id),true);
                    } else {
                        TweetDatabase mTweets = new TweetDatabase(mTwitter.getHome(page,id), context,TweetDatabase.HOME_TL,0);
                        timelineAdapter = new TimelineRecycler(mTweets,(MainActivity)context);
                    }
                    break;

                case TRND:
                    trendsAdapter = (TrendRecycler) trendList.getAdapter();
                    if(trendsAdapter != null && trendsAdapter.getItemCount() > 0)
                        trendsAdapter.getData().setTrends( mTwitter.getTrends(woeid) );
                    else
                        trendsAdapter = new TrendRecycler(new TrendDatabase(mTwitter.getTrends(woeid),context),(MainActivity)context);
                    break;

                case MENT:
                    mentionAdapter = (TimelineRecycler) mentionList.getAdapter();
                    if(mentionAdapter != null && mentionAdapter.getItemCount() != 0) {
                        id = mentionAdapter.getItemId(0);
                        mentionAdapter.getData().insert(mTwitter.getMention(page,id),true);
                    } else {
                        TweetDatabase mention = new TweetDatabase(mTwitter.getMention(page,id), context,TweetDatabase.GET_MENT,0);
                        mentionAdapter = new TimelineRecycler(mention,(MainActivity)context);
                    }
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
            return FAIL;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Integer MODE) {
        switch(MODE) {
            case HOME:
                timelineRefresh.setRefreshing(false);
                if(timelineList.getAdapter().getItemCount() == 0) {
                    timelineList.setAdapter(timelineAdapter);
                } else {
                    timelineAdapter.notifyDataSetChanged();
                }
                break;

            case TRND:
                trendRefresh.setRefreshing(false);
                if(trendList.getAdapter().getItemCount() == 0) {
                    trendList.setAdapter(trendsAdapter);
                } else {
                    trendsAdapter.notifyDataSetChanged();
                }
                break;

            case MENT:
                mentionRefresh.setRefreshing(false);
                if(mentionList.getAdapter().getItemCount() == 0) {
                    mentionList.setAdapter(mentionAdapter);
                } else {
                    mentionAdapter.notifyDataSetChanged();
                }
                break;

            case FAIL:
            default:
                timelineRefresh.setRefreshing(false);
                trendRefresh.setRefreshing(false);
                mentionRefresh.setRefreshing(false);
                Toast.makeText(context, context.getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
        }
    }
}