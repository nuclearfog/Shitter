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

public class MainPage extends AsyncTask<Integer, Void, Void> {

    private final Mode mode;
    private boolean failure = false;

    public MainPage(@NonNull MainActivity context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        tweetDb = new DatabaseAdapter(context);
        woeId = settings.getWoeId();

        tweets = new ArrayList<>();
        trends = new ArrayList<>();
        mention = new ArrayList<>();
        this.mode = mode;

        RecyclerView timelineList = context.findViewById(R.id.tl_list);
        RecyclerView trendList = context.findViewById(R.id.tr_list);
        RecyclerView mentionList = context.findViewById(R.id.m_list);

        timelineAdapter = (TimelineAdapter) timelineList.getAdapter();
        trendsAdapter = (TrendAdapter) trendList.getAdapter();
        mentionAdapter = (TimelineAdapter) mentionList.getAdapter();
    }

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;

    private TimelineAdapter timelineAdapter, mentionAdapter;
    private TrendAdapter trendsAdapter;
    private DatabaseAdapter tweetDb;
    private List<Tweet> tweets, mention;
    private List<Trend> trends;

    private int woeId;

    @Override
    protected Void doInBackground(Integer... args) {
        final int PAGE = args[0];
        long sinceId = 1L;
        try {
            switch (mode) {
                case HOME:
                    if (timelineAdapter.getItemCount() > 0)
                        sinceId = timelineAdapter.getItemId(0);
                    tweets = mTwitter.getHome(PAGE, sinceId);
                    publishProgress();
                    tweetDb.storeHomeTimeline(tweets);
                    break;

                case TRND:
                    trends = mTwitter.getTrends(woeId);
                    publishProgress();
                    tweetDb.storeTrends(trends, woeId);
                    break;

                case MENT:
                    if (mentionAdapter.getItemCount() > 0)
                        sinceId = mentionAdapter.getItemId(0);
                    mention = mTwitter.getMention(PAGE, sinceId);
                    publishProgress();
                    tweetDb.storeMentions(mention);
                    break;

                case DATA:
                    tweets = tweetDb.getHomeTimeline();
                    trends = tweetDb.getTrends(woeId);
                    mention = tweetDb.getMentions();
                    publishProgress();
            }

        } catch (TwitterException err) {
            this.err = err;
            failure = true;
        } catch (Exception err) {
            Log.e("Main Page", err.getMessage());
            failure = true;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... v) {
        disableSwipe();

        switch (mode) {
            case HOME:
                timelineAdapter.setData(tweets);
                timelineAdapter.notifyDataSetChanged();
                break;

            case TRND:
                trendsAdapter.setData(trends);
                trendsAdapter.notifyDataSetChanged();
                break;

            case MENT:
                mentionAdapter.setData(mention);
                mentionAdapter.notifyDataSetChanged();
                break;

            default:
                timelineAdapter.setData(tweets);
                trendsAdapter.setData(trends);
                mentionAdapter.setData(mention);
                timelineAdapter.notifyDataSetChanged();
                trendsAdapter.notifyDataSetChanged();
                mentionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPostExecute(Void v) {
        if (failure && ui.get() != null) {
            disableSwipe();
            if (err != null)
                ErrorHandling.printError(ui.get(), err);
        }
    }

    @Override
    protected void onCancelled() {
        disableSwipe();
    }

    private void disableSwipe() {
        if (ui.get() != null) {
            switch (mode) {
                case HOME:
                    SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
                    timelineRefresh.setRefreshing(false);
                    break;

                case TRND:
                    SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
                    trendRefresh.setRefreshing(false);
                    break;

                case MENT:
                    SwipeRefreshLayout mentionRefresh = ui.get().findViewById(R.id.mention);
                    mentionRefresh.setRefreshing(false);
                    break;
            }
        }
    }


    public enum Mode {
        DATA,
        HOME,
        TRND,
        MENT
    }
}