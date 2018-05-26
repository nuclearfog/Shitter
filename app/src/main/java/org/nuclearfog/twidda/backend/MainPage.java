package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
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
    private int woeid;
    private String errMsg;
    private int highlight, font;
    private boolean image;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        ui = new WeakReference<>((MainActivity)context);
        mTwitter = TwitterEngine.getInstance(context);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        woeid = settings.getInt("woeid",23424829); // Germany WOEID
        highlight = settings.getInt("highlight_color", 0xffff00ff);
        font = settings.getInt("font_color", 0xffffffff);
        image = settings.getBoolean("image_load", true);

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
            DatabaseAdapter tweetDb = new DatabaseAdapter(ui.get());
            TrendDatabase trendDb = new TrendDatabase(ui.get());
            switch (MODE) {
                case HOME:

                    if(timelineAdapter.getItemCount() > 0) {
                        id = timelineAdapter.getItemId(0);
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.store(tweets, DatabaseAdapter.HOME,-1L);
                        tweets.addAll(timelineAdapter.getData());
                    } else {
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.store(tweets, DatabaseAdapter.HOME,-1L);
                    }
                    timelineAdapter.setData(tweets);
                    timelineAdapter.setColor(highlight, font);
                    timelineAdapter.toggleImage(image);
                    break;

                case H_LOAD:

                    DatabaseAdapter tweetDeck = new DatabaseAdapter(ui.get());
                    tweets = tweetDeck.load(DatabaseAdapter.HOME, -1L);
                    timelineAdapter.setData(tweets);
                    timelineAdapter.setColor(highlight, font);
                    timelineAdapter.toggleImage(image);
                    break;

                case TRND:

                    List<Trend> trends = mTwitter.getTrends(woeid);
                    trendDb.store(trends);
                    trendsAdapter.setData(trends);
                    trendsAdapter.setColor(font);
                    break;

                case T_LOAD:

                    trendsAdapter.setData(trendDb.load());
                    trendsAdapter.setColor(font);
                    break;

                case MENT:

                    List<Tweet> mention;
                    if(mentionAdapter.getItemCount() != 0) {
                        id = mentionAdapter.getItemId(0);
                        mention = mTwitter.getMention(page,id);
                        tweetDb.store(mention, DatabaseAdapter.MENT,-1L);
                        mention.addAll(mentionAdapter.getData());
                    } else {
                        mention = mTwitter.getMention(page,id);
                        tweetDb.store(mention, DatabaseAdapter.MENT,-1L);
                    }
                    mentionAdapter.setData(mention);
                    mentionAdapter.setColor(highlight, font);
                    mentionAdapter.toggleImage(image);
                    break;

                case M_LOAD:

                    DatabaseAdapter mentDeck  = new DatabaseAdapter(ui.get());
                    mention = mentDeck.load(DatabaseAdapter.MENT,-1L);
                    mentionAdapter.setData(mention);
                    mentionAdapter.setColor(highlight, font);
                    mentionAdapter.toggleImage(image);
                    break;
            }
        } catch(TwitterException e) {
            errMsg = e.getMessage();
            int errCode = e.getErrorCode();
            if(errCode == 420) {
                int retry = e.getRetryAfter();
                errMsg = "Rate limit erreicht!\n Weiter in "+retry+" Sekunden";
            }
        }
        catch (Exception e) {
            errMsg = e.getMessage();
            if(ui.get() != null) {
                ErrorLog errorLog = new ErrorLog(ui.get());
                errorLog.add(errMsg);
            }
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
                Toast.makeText(connect, "Fehler: "+errMsg, Toast.LENGTH_LONG).show();
            default:
                timelineRefresh.setRefreshing(false);
                trendRefresh.setRefreshing(false);
                mentionRefresh.setRefreshing(false);
        }
    }
}