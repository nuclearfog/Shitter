package org.nuclearfog.twidda.Backend;

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
    private SwipeRefreshLayout tweetsReload, favoritsReload;
    private ListView profileTweets, profileFavorits;
    private TwitterResource twitterResource;
    private TimelineAdapter homeTl, homeFav;

    public ProfileTweets(Context context){
        this.context=context;
        twitterResource = TwitterResource.getInstance(context);
        twitterResource.init();
    }

    /**
     * Profile Backend
     * @see Profile accessing this class
     */
    @Override
    protected void onPreExecute() {
        tweetsReload    = (SwipeRefreshLayout)((Profile)context).findViewById(R.id.hometweets);
        favoritsReload  = (SwipeRefreshLayout)((Profile)context).findViewById(R.id.homefavorits);
        profileTweets   = (ListView)((Profile)context).findViewById(R.id.ht_list);
        profileFavorits = (ListView)((Profile)context).findViewById(R.id.hf_list);
    }

    /**
     * @param id UserID[0]  Mode[1]
     */
    @Override
    protected Void doInBackground(Long... id) {
        try {
            long userId = id[0];
            Twitter twitter = twitterResource.getTwitter();
            if(id[1] == 0) {
                TweetDatabase hTweets = new TweetDatabase(twitter.getUserTimeline(userId), context,TweetDatabase.USER_TL,userId);
                homeTl = new TimelineAdapter(context,hTweets);
            } else if(id[1] == 1) {
               TweetDatabase fTweets = new TweetDatabase(twitter.getFavorites(userId), context,TweetDatabase.FAV_TL,userId);
                homeFav = new TimelineAdapter(context,fTweets);
            }
        } catch(Exception err){err.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if(homeTl != null) {
            profileTweets.setAdapter(homeTl);
            tweetsReload.setRefreshing(false);
        } else if(homeFav != null) {
            profileFavorits.setAdapter(homeFav);
            favoritsReload.setRefreshing(false);
        }
    }
}