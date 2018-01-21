package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.UserDetail;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.User;

public class UserLists extends AsyncTask <Long, Void, Void> {

    private Context context;
    private Twitter twitter;
    private UserAdapter usrAdp;
    private ListView userList;
    private SwipeRefreshLayout userReload;

    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        twitter = TwitterResource.getInstance(context).getTwitter();
        userList = (ListView)((UserDetail)context).findViewById(R.id.followList);
        userReload = (SwipeRefreshLayout)((UserDetail)context).findViewById(R.id.follow_swipe);
    }

    /**
     * @param data [0] mode UserLists/UserDetail ,  [1] UserID
     */
    @Override
    protected Void doInBackground(Long... data) {
        long mode = data[0];
        long id = data[1];
        long cursor = -1L;  //TODO
        List<User> userlist = null;
        try {
            if(mode == 0L) { //FOLLOWING
                userlist = twitter.getFollowersList(id,cursor);
            } else if(mode == 1L) { //Follower
                userlist = twitter.getFriendsList(id,cursor);
            } else if(mode == 2L) {         // Retweet TODO
            } else if(mode == 3L) {         // Favorite TODO
            }
            if(userlist != null)
                usrAdp = new UserAdapter(context,new UserDatabase(context,userlist));
        } catch(Exception err) {
            err.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        userList.setAdapter(usrAdp);
        userReload.setRefreshing(false);
    }
}