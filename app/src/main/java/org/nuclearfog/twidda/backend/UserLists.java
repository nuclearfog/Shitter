package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.UserDetail;

public class UserLists extends AsyncTask <Long, Void, Void> {

    private Context context;
    private TwitterEngine mTwitter;
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
        mTwitter = TwitterEngine.getInstance(context);
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
        try {
            if(mode == 0L) { // GET FOLLOWING USERS
                usrAdp = new UserAdapter(context,new UserDatabase(context,mTwitter.getFollowing(id)));
            }
            else if(mode == 1L) { // GET FOLLOWER
                usrAdp = new UserAdapter(context,new UserDatabase(context,mTwitter.getFollower(id)));
            }
            else if(mode == 2L) { // GET RETWEET USER
            }
            else if(mode == 3L) { // GET FAV USERS TODO
            }
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