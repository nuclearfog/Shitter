package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
    private TwitterException err;

    private TimelineAdapter timelineAdapter, mentionAdapter;
    private TrendAdapter trendsAdapter;
    private DatabaseAdapter tweetDb;
    private List<Tweet> tweets, mention;
    private List<Trend> trends;

    private int woeId;


    public MainPage(@NonNull MainActivity context) {
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
            switch (MODE) {
                case HOME:
                    if (timelineAdapter.getItemCount() > 0)
                        sinceId = timelineAdapter.getItemId(0);
                    tweets = mTwitter.getHome(PAGE, sinceId);
                    publishProgress(HOME);
                    tweetDb.storeHomeTimeline(tweets);
                    break;

                case TRND:
                    trends = mTwitter.getTrends(woeId);
                    publishProgress(TRND);
                    tweetDb.storeTrends(trends, woeId);
                    break;

                case MENT:
                    if (mentionAdapter.getItemCount() != 0)
                        sinceId = mentionAdapter.getItemId(0);
                    mention = mTwitter.getMention(PAGE, sinceId);
                    publishProgress(MENT);
                    tweetDb.storeMentions(mention);
                    break;

                default:
                    tweets = tweetDb.getHomeTimeline();
                    publishProgress(HOME);
                    trends = tweetDb.getTrends(woeId);
                    publishProgress(TRND);
                    mention = tweetDb.getMentions();
                    publishProgress(MENT);
            }
        } catch (TwitterException err) {
            this.err = err;
            return FAIL;
        } catch (Exception err) {
            Log.e("Main Page", err.getMessage());
            return FAIL;
        }
        return MODE;
    }


    @Override
    protected void onProgressUpdate(Integer... modes) {
        if (ui.get() == null) return;

        final int MODE = modes[0];

        switch (MODE) {
            case HOME:
                timelineAdapter.setData(tweets);
                timelineAdapter.notifyDataSetChanged();
                SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
                timelineRefresh.setRefreshing(false);
                break;

            case TRND:
                trendsAdapter.setData(trends);
                trendsAdapter.notifyDataSetChanged();
                SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
                trendRefresh.setRefreshing(false);
                break;

            case MENT:
                mentionAdapter.setData(mention);
                mentionAdapter.notifyDataSetChanged();
                SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);
                mentionRefresh.setRefreshing(false);
                break;
        }
    }


    @Override
    protected void onPostExecute(Integer result) {
        if (ui.get() == null) return;

        if (result == FAIL) {
            SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
            SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
            SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);

            timelineRefresh.setRefreshing(false);
            trendRefresh.setRefreshing(false);
            mentionRefresh.setRefreshing(false);

            if (err != null) {
                ErrorHandling.printError(ui.get(), err);
            }
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
        SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
        SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);

        timelineRefresh.setRefreshing(false);
        trendRefresh.setRefreshing(false);
        mentionRefresh.setRefreshing(false);
    }
}