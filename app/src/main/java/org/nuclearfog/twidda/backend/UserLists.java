package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.UserRecycler;
import org.nuclearfog.twidda.window.UserDetail;

import java.lang.ref.WeakReference;

public class UserLists extends AsyncTask <Long, Void, Void> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private UserRecycler usrAdp;
    private RecyclerView userList;
    private ProgressBar uProgress;
    private String errmsg;

    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        ui = new WeakReference<>((UserDetail)context);
        mTwitter = TwitterEngine.getInstance(context);
        userList = (RecyclerView) ui.get().findViewById(R.id.userlist);
        uProgress = (ProgressBar) ui.get().findViewById(R.id.user_progress);
    }



    @Override
    protected Void doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            usrAdp = (UserRecycler) userList.getAdapter();
            if(mode == FOLLOWING) {
                if(usrAdp == null) {
                    UserDatabase udb = new UserDatabase(ui.get(),mTwitter.getFollowing(id,cursor));
                    usrAdp = new UserRecycler(udb,ui.get());
                } else {
                    UserDatabase uDb = usrAdp.getData();
                    uDb.addLast(mTwitter.getFollowing(id,cursor));
                    usrAdp.notifyDataSetChanged();
                }
            }
            else if(mode == FOLLOWERS) {
                if(usrAdp == null) {
                    UserDatabase udb = new UserDatabase(ui.get(),mTwitter.getFollower(id,cursor));
                    usrAdp = new UserRecycler(udb,ui.get());
                } else {
                    UserDatabase uDb = usrAdp.getData();
                    uDb.addLast(mTwitter.getFollower(id,cursor));
                    usrAdp.notifyDataSetChanged();
                }
            }
            else if(mode == RETWEETER) {
                UserDatabase udb = new UserDatabase(ui.get(),mTwitter.getRetweeter(id,cursor));
                usrAdp = new UserRecycler(udb,ui.get());
            }
            /*else if(mode == FAVORISER) {
                // GET FAV USERS TODO
            }*/
        }
        catch(Exception err) {
            errmsg = "Fehler: "+err.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if(ui.get() == null)
            return;
        if(errmsg == null) {
            userList.setAdapter(usrAdp);
        } else {
            Toast.makeText(ui.get().getApplicationContext(),errmsg,Toast.LENGTH_LONG).show();
        }
        uProgress.setVisibility(View.INVISIBLE);
    }
}