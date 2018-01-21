package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.UserDetail;

import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.User;

public class UserLists extends AsyncTask <Long, Void, Void> {

    private Context context;
    private Twitter twitter;
    private UserAdapter usrAdp;
    private ListView userList;
    private ProgressBar uProgress;

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
        uProgress = (ProgressBar)((UserDetail)context).findViewById(R.id.user_progress);
    }

    /**
     * @param data [0] mode UserLists/UserDetail ,  [1] UserID
     */
    @Override
    protected Void doInBackground(Long... data) {
        long mode = data[0];
        long id = data[1];
        long cursor = -1L;  //TODO
        List<User> listUser = null;
        try {
            if(mode == 0L) { // GET FOLLOWING USERS
                listUser = twitter.getFriendsList(id,cursor);
            }
            else if(mode == 1L) { // GET FOLLOWER
                listUser = twitter.getFollowersList(id,cursor);
            }
            else if(mode == 2L) { // GET RETWEET USER
                IDs retweeter = twitter.getRetweeterIds(id, cursor);
                listUser = new ArrayList<>();
                for(long userId : retweeter.getIDs()) {
                    listUser.add(twitter.showUser(userId));
                }
            }
            else if(mode == 3L) { // GET FAV USERS TODO
            }
            if(listUser != null)
                usrAdp = new UserAdapter(context,new UserDatabase(context,listUser));
        }
        catch(Exception err) {
            err.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        userList.setAdapter(usrAdp);
        uProgress.setVisibility(View.INVISIBLE);
    }
}