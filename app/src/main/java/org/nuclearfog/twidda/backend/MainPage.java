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
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.window.ColorPreferences;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainPage extends AsyncTask<Integer, Void, Integer> {

    public static final int HOME = 0;
    public static final int TRND = 1;
    public static final int MENT = 2;
    private static final int FAIL = -1;

    private WeakReference<MainActivity> ui;
    private TwitterEngine mTwitter;
    private RecyclerView timelineList, trendList, mentionList;
    private TimelineRecycler timelineAdapter, mentionAdapter;
    private TrendRecycler trendsAdapter;
    private int woeid;
    private String errMsg;
    private int highlight, font;

    /**
     * Main View
     * @see MainActivity
     */
    public MainPage(Context context) {
        ui = new WeakReference<>((MainActivity)context);
        mTwitter = TwitterEngine.getInstance(context);
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        woeid = settings.getInt("woeid",23424829); // Germany WOEID
        timelineList = (RecyclerView)ui.get().findViewById(R.id.tl_list);
        trendList = (RecyclerView)ui.get().findViewById(R.id.tr_list);
        mentionList = (RecyclerView)ui.get().findViewById(R.id.m_list);
        ColorPreferences mColor = ColorPreferences.getInstance(ui.get());
        highlight = mColor.getColor(ColorPreferences.HIGHLIGHTING);
        font = mColor.getColor(ColorPreferences.FONT_COLOR);
    }

    /**
     * @param args [0] Execution Mode: (0)HomeTL, (1)Trend, (2)Mention
     * @return success
     */
    @Override
    protected Integer doInBackground(Integer... args) {
        final int MODE = args[0];
        int page = args[1];
        long id = 1L;
        try {
            TweetDatabase tweetDb = new TweetDatabase(ui.get());
            switch (MODE) {
                case HOME:
                    List<Tweet> tweets;
                    timelineAdapter = (TimelineRecycler) timelineList.getAdapter();
                    if(timelineAdapter != null && timelineAdapter.getItemCount() > 0) {
                        id = timelineAdapter.getItemId(0);
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.store(tweets, TweetDatabase.HOME,-1L);
                        tweets.addAll(timelineAdapter.getData());
                    } else {
                        tweets = mTwitter.getHome(page,id);
                        tweetDb.store(tweets, TweetDatabase.HOME,-1L);
                    }
                    timelineAdapter = new TimelineRecycler(tweets, ui.get());
                    timelineAdapter.setColor(highlight, font);
                    break;

                case TRND:
                    trendsAdapter = (TrendRecycler) trendList.getAdapter();
                    if(trendsAdapter != null && trendsAdapter.getItemCount() > 0)
                        trendsAdapter.getData().setTrends( mTwitter.getTrends(woeid) );
                    else
                        trendsAdapter = new TrendRecycler(new TrendDatabase(mTwitter.getTrends(woeid),ui.get()), ui.get());
                    trendsAdapter.setColor(font);
                    break;

                case MENT:
                    List<Tweet> mention;
                    mentionAdapter = (TimelineRecycler) mentionList.getAdapter();
                    if(mentionAdapter != null && mentionAdapter.getItemCount() != 0) {
                        id = mentionAdapter.getItemId(0);
                        mention = mTwitter.getMention(page,id);
                        tweetDb.store(mention,TweetDatabase.MENT,-1L);
                        mention.addAll(mentionAdapter.getData());
                    } else {
                        mention = mTwitter.getMention(page,id);
                        tweetDb.store(mention,TweetDatabase.MENT,-1L);
                    }
                    mentionAdapter = new TimelineRecycler(mention, ui.get());
                    mentionAdapter.setColor(highlight, font);
                    break;
            }
        } catch (Exception e){
            errMsg = e.getMessage();
            return FAIL;
        }
        return MODE;
    }

    @Override
    protected void onPostExecute(Integer MODE) {
        MainActivity connect = ui.get();
        if(connect == null)
            return;
        Context context = connect.getApplicationContext();
        SwipeRefreshLayout timelineRefresh = (SwipeRefreshLayout)connect.findViewById(R.id.timeline);
        SwipeRefreshLayout trendRefresh = (SwipeRefreshLayout)connect.findViewById(R.id.trends);
        SwipeRefreshLayout mentionRefresh = (SwipeRefreshLayout)connect.findViewById(R.id.mention);

        switch(MODE) {
            case HOME:
                timelineList.setAdapter(timelineAdapter);
                timelineRefresh.setRefreshing(false);
                break;

            case TRND:
                trendRefresh.setRefreshing(false);
                if(trendList.getAdapter().getItemCount() == 0)
                    trendList.setAdapter(trendsAdapter);
                else
                    trendsAdapter.notifyDataSetChanged();
                break;

            case MENT:
                mentionList.setAdapter(mentionAdapter);
                mentionRefresh.setRefreshing(false);
                break;

            case FAIL:
            default:
                timelineRefresh.setRefreshing(false);
                trendRefresh.setRefreshing(false);
                mentionRefresh.setRefreshing(false);
                Toast.makeText(context, "Fehler: "+errMsg, Toast.LENGTH_LONG).show();
        }
    }
}