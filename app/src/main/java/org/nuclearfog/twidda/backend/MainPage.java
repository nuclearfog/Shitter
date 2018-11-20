package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.TimelineAdapter;
import org.nuclearfog.twidda.adapter.TrendAdapter;
import org.nuclearfog.twidda.backend.items.Trend;
import org.nuclearfog.twidda.backend.items.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class MainPage extends AsyncTask<Integer, Integer, Integer> {

    public static final int DATA = 0;
    public static final int HOME = 1;
    public static final int TRND = 2;
    public static final int MENT = 3;
    private static final int FAIL = 4;

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;

    private TimelineAdapter timelineAdapter, mentionAdapter;
    private TrendAdapter trendsAdapter;
    private DatabaseAdapter tweetDb;
    private List<Tweet> tweets, mention;
    private List<Trend> trends;

    private int woeId;
    private String errMsg = "E Main Page: ";
    private int returnCode = 0;


    public MainPage(MainActivity context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        tweetDb = new DatabaseAdapter(context);
        woeId = settings.getWoeId();

        tweets = new ArrayList<>();
        trends = new ArrayList<>();
        mention = new ArrayList<>();

        RecyclerView timelineList = context.findViewById(R.id.tl_list);
        RecyclerView trendList = context.findViewById(R.id.tr_list);
        RecyclerView mentionList = context.findViewById(R.id.m_list);

        timelineAdapter = (TimelineAdapter) timelineList.getAdapter();
        trendsAdapter = (TrendAdapter) trendList.getAdapter();
        mentionAdapter = (TimelineAdapter) mentionList.getAdapter();
    }


    @Override
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        final int PAGE = args[1];
        long sinceId = 1L;
        try {
            if (MODE == HOME) {
                if (timelineAdapter.getItemCount() > 0)
                    sinceId = timelineAdapter.getItemId(0);
                tweets = mTwitter.getHome(PAGE, sinceId);
                publishProgress(HOME);
                tweetDb.storeHomeTimeline(tweets);
            } else if (MODE == TRND) {
                trends = mTwitter.getTrends(woeId);
                publishProgress(TRND);
                tweetDb.storeTrends(trends, woeId);
            } else if (MODE == MENT) {
                if (mentionAdapter.getItemCount() != 0)
                    sinceId = mentionAdapter.getItemId(0);
                mention = mTwitter.getMention(PAGE, sinceId);
                publishProgress(MENT);
                tweetDb.storeMentions(mention);
            } else {
                tweets = tweetDb.getHomeTimeline();
                publishProgress(HOME);
                trends = tweetDb.getTrends(woeId);
                publishProgress(TRND);
                mention = tweetDb.getMentions();
                publishProgress(MENT);
            }
        } catch (TwitterException e) {
            returnCode = e.getErrorCode();
            errMsg += e.getMessage();
            return FAIL;
        } catch (Exception e) {
            e.printStackTrace();
            errMsg += e.getMessage();
            Log.e("Main Page", errMsg);
            return FAIL;
        }
        return MODE;
    }


    @Override
    protected void onProgressUpdate(Integer... modes) {
        if (ui.get() == null) return;

        final int MODE = modes[0];
        if (MODE == HOME) {
            timelineAdapter.setData(tweets);
            timelineAdapter.notifyDataSetChanged();
            SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
            timelineRefresh.setRefreshing(false);
        } else if (MODE == TRND) {
            trendsAdapter.setData(trends);
            trendsAdapter.notifyDataSetChanged();
            SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
            trendRefresh.setRefreshing(false);
        } else if (MODE == MENT) {
            mentionAdapter.setData(mention);
            mentionAdapter.notifyDataSetChanged();
            SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);
            mentionRefresh.setRefreshing(false);
        }
    }


    @Override
    protected void onPostExecute(Integer mode) {
        if (ui.get() == null) return;

        if (mode == FAIL) {
            SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
            SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
            SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);

            timelineRefresh.setRefreshing(false);
            trendRefresh.setRefreshing(false);
            mentionRefresh.setRefreshing(false);

            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(ui.get(), R.string.error_not_specified, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(ui.get(), errMsg, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCancelled(Integer i) {
        if (ui.get() == null) return;

        SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
        SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
        SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);

        timelineRefresh.setRefreshing(false);
        trendRefresh.setRefreshing(false);
        mentionRefresh.setRefreshing(false);
    }
}