package org.nuclearfog.twidda.engine;

import android.content.Context;
import android.os.AsyncTask;

import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.engine.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.engine.ViewAdapter.TrendsAdapter;

public class TwitterEngine extends AsyncTask<Integer, Void, Void>
{
    private final String TWITTER_CONSUMER_KEY = "GrylGIgQK3cDjo9mSTBqF1vwf";
    private final String TWITTER_CONSUMER_SECRET = "pgaWUlDVS5b7Q6VJQDgBzHKw0mIxJIX0UQBcT1oFJEivsCl5OV";
    private final String ERR_MSG = "Fehler bei der Verbindung";

    private static Twitter twitter;
    private Context context;
    private ListView list;
    private TimelineAdapter timelineAdapter;
    private TrendsAdapter trendsAdapter;
    private SwipeRefreshLayout refresh;

    public TwitterEngine(Context context, ListView list) {
        this.context=context;
        this.list = list;
        if(twitter == null) init();
    }
    public void setRefresh(SwipeRefreshLayout refresh) {
        this.refresh=refresh;
    }

    @Override
    protected Void doInBackground(Integer... args) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        // twitter.getRateLimitStatus();

        try {
            switch(args[0]) {
                case (0): // Home Timeline
                    timelineAdapter = new TimelineAdapter(context,R.layout.tweet,twitter.getHomeTimeline());
                    break;
                case(1):  // Trends
                    Trends trend = twitter.getPlaceTrends(1);
                    trendsAdapter = new TrendsAdapter(context,R.layout.tweet,trend.getTrends());
                    break;
                case(2):  // Mentions
                    // TODO
                    break;
            }
        } catch (TwitterException e) {
            Toast.makeText(context, ERR_MSG, Toast.LENGTH_SHORT).show();
        } catch (Exception e){ e.printStackTrace(); }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if(timelineAdapter != null)
            list.setAdapter(timelineAdapter);
        else if(trendsAdapter != null)
            list.setAdapter(trendsAdapter);
        refresh.setRefreshing(false);
    }

    /**
     * Init Twitter
     */
    private void init() {
        SharedPreferences einstellungen = context.getSharedPreferences("settings", 0);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
        String accessToken = einstellungen.getString("accesstoken","");
        String accessTokenSec = einstellungen.getString("accesstokensecret","");
        AccessToken token = new AccessToken(accessToken,accessTokenSec);
        twitter = new TwitterFactory( builder.build() ).getInstance(token);
    }
}