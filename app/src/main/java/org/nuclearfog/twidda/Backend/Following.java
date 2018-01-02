package org.nuclearfog.twidda.Backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.nuclearfog.twidda.DataBase.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.UserAdapter;
import org.nuclearfog.twidda.Window.Follower;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.User;

public class Following extends AsyncTask <Long, Void, Void> {

    private Context context;
    private Twitter twitter;
    private UserAdapter usrAdp;
    private ListView followList;
    private SwipeRefreshLayout followReload;

    public Following(Context context) {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        twitter = TwitterResource.getInstance(context).getTwitter();
        followList = (ListView)((Follower)context).findViewById(R.id.followList);
        followReload = (SwipeRefreshLayout)((Follower)context).findViewById(R.id.follow_swipe);
    }

    /**
     * @param data [0] mode Following/Follower ,  [1] UserID
     */
    @Override
    protected Void doInBackground(Long... data) {
        long mode = data[0];
        long userID = data[1];
        long cursor = -1L;  //TODO
        List<User> userlist;
        try {
            if(mode == 1L) { //FOLLOWING
                userlist = twitter.getFollowersList(userID,cursor);
                usrAdp = new UserAdapter(context,new UserDatabase(context,userlist));
            } else {        //Follower
                userlist = twitter.getFriendsList(userID,cursor);
                usrAdp = new UserAdapter(context,new UserDatabase(context,userlist));
            }
        } catch(Exception err) {
            err.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        followList.setAdapter(usrAdp);
        followReload.setRefreshing(false);
    }
}