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


public class MainPage extends AsyncTask<Integer, Void, Integer> {

    public static final int HOME = 0;
    public static final int TRND = 1;
    public static final int MENT = 2;
    private static final int FAIL = -1;

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
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        int page = args[1];
        long id = 1L;
        try {
            switch (MODE) {
                case HOME:
                timelineAdapter = (TimelineAdapter) timelineList.getAdapter();
                if(timelineAdapter.getCount() == 0) {
                    TweetDatabase mTweets = new TweetDatabase(mTwitter.getHome(page,id), context,TweetDatabase.HOME_TL,0);
                    timelineAdapter = new TimelineAdapter(context,mTweets);
                } else {
                    id = timelineAdapter.getItemId(0);
                    timelineAdapter.getData().add(mTwitter.getHome(page,id));
                }
                break;

                case TRND:
                trendsAdapter = new TrendAdapter(context, new TrendDatabase(mTwitter.getTrends(),context));
                break;

                case MENT:
                mentionAdapter = (TimelineAdapter) mentionList.getAdapter();
                if(mentionAdapter.getCount() == 0) {
                    TweetDatabase mention = new TweetDatabase(mTwitter.getMention(page,id), context,TweetDatabase.GET_MENT,0);
                    mentionAdapter = new TimelineAdapter(context,mention);
                } else {
                    id = mentionAdapter.getItemId(0);
                    mentionAdapter.getData().add(mTwitter.getMention(page,id));
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
                if(timelineList.getAdapter().getCount() == 0) {
                    timelineList.setAdapter(timelineAdapter);
                } else {
                    timelineAdapter.notifyDataSetChanged();
                }
                break;

            case TRND:
                trendRefresh.setRefreshing(false);
                trendList.setAdapter(trendsAdapter);
                break;

            case MENT:
                mentionRefresh.setRefreshing(false);
                if(mentionList.getAdapter().getCount() == 0) {
                    mentionList.setAdapter(mentionAdapter);
                } else {
                    mentionAdapter.notifyDataSetChanged();
                }
                break;

            case FAIL:
            default:
                Toast.makeText(context, context.getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
        }
    }
}