package org.nuclearfog.twidda.Engine;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;
import org.nuclearfog.twidda.Window.Profile;

import twitter4j.Twitter;

public class ProfileTweets extends AsyncTask<Long, Void, Void> {

    private Context context;
    private SwipeRefreshLayout refreshHome;
    private ListView profileList;
    private TwitterStore twitterStore;
    private TimelineAdapter homeTl;

    public ProfileTweets(Context context){
        this.context=context;
        twitterStore = TwitterStore.getInstance(context);
        twitterStore.init();
    }

    @Override
    protected void onPreExecute(){
        //refreshHome = (SwipeRefreshLayout)((Profile)context).findViewById(R.id.refreshHome);
        //profileList = (ListView)((Profile)context).findViewById(R.id.home_tl);
    }

    @Override
    protected Void doInBackground(Long... id) {
        try {
            Twitter twitter = twitterStore.getTwitter();
            long userId = id[0];
            TweetDatabase hTweets = new TweetDatabase(twitter.getUserTimeline(userId), context,TweetDatabase.USER_TL);
            homeTl = new TimelineAdapter(context,R.layout.tweet,hTweets);
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v){
        profileList.setAdapter(homeTl);
        refreshHome.setRefreshing(false);
    }
}
