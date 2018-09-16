package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Trend;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;
import org.nuclearfog.twidda.viewadapter.TrendAdapter;

import java.lang.ref.WeakReference;
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
    private int woeId;
    private String errMsg = "E Main Page: ";
    private int returnCode = 0;


    public MainPage(MainActivity context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        tweetDb = new DatabaseAdapter(context);
        woeId = settings.getWoeId();
        int highlight = settings.getHighlightColor();
        int font = settings.getFontColor();
        boolean image = settings.loadImages();

        RecyclerView timelineList = context.findViewById(R.id.tl_list);
        RecyclerView trendList = context.findViewById(R.id.tr_list);
        RecyclerView mentionList = context.findViewById(R.id.m_list);
        timelineAdapter = (TimelineAdapter) timelineList.getAdapter();
        trendsAdapter = (TrendAdapter) trendList.getAdapter();
        mentionAdapter = (TimelineAdapter) mentionList.getAdapter();

        if (timelineAdapter == null) {
            timelineAdapter = new TimelineAdapter(context);
            timelineAdapter.setColor(highlight, font);
            timelineAdapter.toggleImage(image);
            timelineList.setAdapter(timelineAdapter);
        }
        if (trendsAdapter == null) {
            trendsAdapter = new TrendAdapter(context);
            trendsAdapter.setColor(font);
            trendList.setAdapter(trendsAdapter);
        }
        if (mentionAdapter == null) {
            mentionAdapter = new TimelineAdapter(context);
            mentionAdapter.setColor(highlight, font);
            mentionAdapter.toggleImage(image);
            mentionList.setAdapter(mentionAdapter);
        }
    }


    @Override
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        final int PAGE = args[1];
        try {
            if (MODE == HOME) {
                List<Tweet> tweets;
                if (timelineAdapter.getItemCount() > 0) {
                    long id = timelineAdapter.getItemId(0);
                    tweets = mTwitter.getHome(PAGE, id);
                    timelineAdapter.addNew(tweets);
                } else {
                    tweets = mTwitter.getHome(PAGE, 1L);
                    timelineAdapter.setData(tweets);
                }
                publishProgress(HOME);
                tweetDb.storeHomeTimeline(tweets);
            } else if (MODE == TRND) {
                List<Trend> trends = mTwitter.getTrends(woeId);
                trendsAdapter.setData(trends);
                publishProgress(TRND);
                tweetDb.store(trends, woeId);
            } else if (MODE == MENT) {
                List<Tweet> tweets;
                if (mentionAdapter.getItemCount() != 0) {
                    long id = mentionAdapter.getItemId(0);
                    tweets = mTwitter.getMention(PAGE, id);
                    mentionAdapter.addNew(tweets);
                } else {
                    tweets = mTwitter.getMention(PAGE, 1L);
                    mentionAdapter.setData(tweets);
                }
                publishProgress(MENT);
                tweetDb.storeMentions(tweets);
            } else {
                List<Tweet> tweets = tweetDb.getHomeTimeline();
                timelineAdapter.setData(tweets);
                publishProgress(HOME);

                trendsAdapter.setData(tweetDb.load(woeId));
                publishProgress(TRND);

                tweets = tweetDb.getMentions();
                mentionAdapter.setData(tweets);
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
            timelineAdapter.notifyDataSetChanged();
            SwipeRefreshLayout timelineRefresh = ui.get().findViewById(R.id.timeline);
            timelineRefresh.setRefreshing(false);
        } else if (MODE == TRND) {
            trendsAdapter.notifyDataSetChanged();
            SwipeRefreshLayout trendRefresh = ui.get().findViewById(R.id.trends);
            trendRefresh.setRefreshing(false);
        } else if (MODE == MENT) {
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
}