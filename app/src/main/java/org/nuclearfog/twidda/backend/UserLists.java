package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserAdapter;
import org.nuclearfog.twidda.window.UserDetail;

public class UserLists extends AsyncTask <Long, Void, Void> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private Context context;
    private TwitterEngine mTwitter;
    private UserAdapter usrAdp;
    private ListView userList;
    private ProgressBar uProgress;
    private String errmsg;

    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        mTwitter = TwitterEngine.getInstance(context);
        userList = (ListView)((UserDetail)context).findViewById(R.id.userlist);
        uProgress = (ProgressBar)((UserDetail)context).findViewById(R.id.user_progress);
    }

    /**
     * @param data [0] mode UserLists/UserDetail ,  [1] UserID
     */
    @Override
    protected Void doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            usrAdp = (UserAdapter) userList.getAdapter();
            if(mode == FOLLOWING) {
                if(usrAdp == null) {
                    usrAdp = new UserAdapter(context,new UserDatabase(context,mTwitter.getFollowing(id,cursor)));
                } else {
                    UserDatabase uDb = usrAdp.getData();
                    uDb.addLast(mTwitter.getFollowing(id,cursor));
                    usrAdp.notifyDataSetChanged();
                }
            }
            else if(mode == FOLLOWERS) {
                if(usrAdp == null) {
                    usrAdp = new UserAdapter(context,new UserDatabase(context,mTwitter.getFollower(id,cursor)));
                } else {
                    UserDatabase uDb = usrAdp.getData();
                    uDb.addLast(mTwitter.getFollower(id,cursor));
                    usrAdp.notifyDataSetChanged();
                }
            }
            else if(mode == RETWEETER) {
                // GET RETWEET USER TODO
            }
            else if(mode == FAVORISER) {
                // GET FAV USERS TODO
            }
        }
        catch(Exception err) {
            errmsg = err.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if(errmsg == null) {
            userList.setAdapter(usrAdp);
            uProgress.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(context,errmsg,Toast.LENGTH_LONG).show();
        }
    }
}