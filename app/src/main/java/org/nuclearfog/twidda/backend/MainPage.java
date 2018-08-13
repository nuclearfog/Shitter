package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import org.nuclearfog.twidda.MainActivity;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.Trend;
import org.nuclearfog.twidda.backend.listitems.Tweet;
import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.database.TrendDatabase;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.viewadapter.TrendRecycler;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class MainPage extends AsyncTask<Integer, Void, Integer> {

    public static final int HOME = 0;
    public static final int TRND = 1;
    public static final int MENT = 2;
    public static final int H_LOAD = 3;
    public static final int T_LOAD = 4;
    public static final int M_LOAD = 5;
    private static final int FAIL = -1;

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;

    private TimelineRecycler timelineAdapter, mentionAdapter;
    private TrendRecycler trendsAdapter;
    private DatabaseAdapter tweetDb;
    private ErrorLog errorLog;
    private int woeId;
    private int highlight, font;
    private boolean image;
    private String errMsg = "E: Main Page, ";
    private int returnCode = 0;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        ui = new WeakReference<>((MainActivity)context);
        mTwitter = TwitterEngine.getInstance(context);
        GlobalSettings settings = GlobalSettings.getInstance(context);
        tweetDb = new DatabaseAdapter(ui.get());
        errorLog = new ErrorLog(context);
        woeId = settings.getWoeId();
        highlight = settings.getHighlightColor();
        font = settings.getFontColor();
        image = settings.loadImages();

        RecyclerView timelineList = ui.get().findViewById(R.id.tl_list);
        RecyclerView trendList = ui.get().findViewById(R.id.tr_list);
        RecyclerView mentionList = ui.get().findViewById(R.id.m_list);
        timelineAdapter = (TimelineRecycler) timelineList.getAdapter();
        trendsAdapter = (TrendRecycler) trendList.getAdapter();
        mentionAdapter = (TimelineRecycler) mentionList.getAdapter();

        if(timelineAdapter == null) {
            timelineAdapter = new TimelineRecycler(ui.get());
            timelineList.setAdapter(timelineAdapter);
        }
        if(trendsAdapter == null) {
            trendsAdapter = new TrendRecycler(ui.get());
            trendList.setAdapter(trendsAdapter);
        }
        if(mentionAdapter == null) {
            mentionAdapter = new TimelineRecycler(ui.get());
            mentionList.setAdapter(mentionAdapter);
        }
    }

    /**
     * @param args [0] Execution Mode: (0)HomeTL, (1)Trend, (2)Mention
     * @return Mode
     */
    @Override
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        int page = args[1];
        long id = 1L;
        List<Tweet> tweets;
        try {
            TrendDatabase trendDb = new TrendDatabase(ui.get());
            switch (MODE) {
                case HOME:

                    if(timelineAdapter.getItemCount() > 0) {
                        id = timelineAdapter.getItemId(0);
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.storeHomeTimeline(tweets);
                        tweets.addAll(timelineAdapter.getData());
                    } else {
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.storeHomeTimeline(tweets);
                    }
                    timelineAdapter.setData(tweets);
                    timelineAdapter.setColor(highlight, font);
                    timelineAdapter.toggleImage(image);
                    break;

                case H_LOAD:

                    DatabaseAdapter tweetDeck = new DatabaseAdapter(ui.get());
                    tweets = tweetDeck.getHomeTimeline();
                    timelineAdapter.setData(tweets);
                    timelineAdapter.setColor(highlight, font);
                    timelineAdapter.toggleImage(image);
                    break;

                case TRND:

                    List<Trend> trends = mTwitter.getTrends(woeId);
                    trendDb.store(trends, woeId);
                    trendsAdapter.setData(trends);
                    trendsAdapter.setColor(font);
                    break;

                case T_LOAD:

                    trendsAdapter.setData(trendDb.load(woeId));
                    trendsAdapter.setColor(font);
                    break;

                case MENT:

                    List<Tweet> mention;
                    if(mentionAdapter.getItemCount() != 0) {
                        id = mentionAdapter.getItemId(0);
                        mention = mTwitter.getMention(page,id);
                        tweetDb.storeMentions(mention);
                        mention.addAll(mentionAdapter.getData());
                    } else {
                        mention = mTwitter.getMention(page,id);
                        tweetDb.storeMentions(mention);
                    }
                    mentionAdapter.setData(mention);
                    mentionAdapter.setColor(highlight, font);
                    mentionAdapter.toggleImage(image);
                    break;

                case M_LOAD:

                    DatabaseAdapter mentDeck  = new DatabaseAdapter(ui.get());
                    mention = mentDeck.getMentions();
                    mentionAdapter.setData(mention);
                    mentionAdapter.setColor(highlight, font);
                    mentionAdapter.toggleImage(image);
                    break;
            }
        } catch(TwitterException e) {
            returnCode = e.getErrorCode();
            if (returnCode > 0 && returnCode != 420) {
                errMsg += e.getMessage();
            }
            return FAIL;
        }
        catch (Exception e) {
            errMsg += e.getMessage();
            errorLog.add(errMsg);
            return FAIL;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Integer MODE) {
        MainActivity connect = ui.get();
        if(connect == null)
            return;
        SwipeRefreshLayout timelineRefresh = connect.findViewById(R.id.timeline);
        SwipeRefreshLayout trendRefresh = connect.findViewById(R.id.trends);
        SwipeRefreshLayout mentionRefresh = connect.findViewById(R.id.mention);

        switch(MODE) {
            case HOME:
            case H_LOAD:
                timelineAdapter.notifyDataSetChanged();
                timelineRefresh.setRefreshing(false);
                break;

            case TRND:
            case T_LOAD:
                trendsAdapter.notifyDataSetChanged();
                trendRefresh.setRefreshing(false);
                break;

            case MENT:
            case M_LOAD:
                mentionAdapter.notifyDataSetChanged();
                mentionRefresh.setRefreshing(false);
                break;

            case FAIL:
                if (returnCode > 0) {
                    if (returnCode == 420) {
                        Toast.makeText(connect, R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(connect, errMsg, Toast.LENGTH_LONG).show();
                    }
                }
            default:
                timelineRefresh.setRefreshing(false);
                trendRefresh.setRefreshing(false);
                mentionRefresh.setRefreshing(false);
        }
    }
}