package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.listitems.TwitterUser;
import org.nuclearfog.twidda.database.ErrorLog;
import org.nuclearfog.twidda.viewadapter.UserRecycler;
import org.nuclearfog.twidda.window.UserDetail;

import java.lang.ref.WeakReference;
import java.util.List;

public class UserLists extends AsyncTask <Long, Void, Void> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private UserRecycler usrAdp;
    private RecyclerView userList;
    private String errmsg;
    private boolean imageload;


    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        ui = new WeakReference<>((UserDetail)context);
        mTwitter = TwitterEngine.getInstance(context);
        userList = ui.get().findViewById(R.id.userlist);

        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        imageload = settings.getBoolean("image_load",true);
    }


    @Override
    protected Void doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            usrAdp = (UserRecycler) userList.getAdapter();
            if(mode == FOLLOWING) {
                List<TwitterUser> user = mTwitter.getFollowing(id,cursor);
                usrAdp = new UserRecycler(user,ui.get());
            }
            else if(mode == FOLLOWERS) {
                List<TwitterUser> user = mTwitter.getFollower(id,cursor);
                usrAdp = new UserRecycler(user,ui.get());
            }
            else if(mode == RETWEETER) {
                List<TwitterUser> user = mTwitter.getRetweeter(id,cursor);
                usrAdp = new UserRecycler(user,ui.get());
            }
            usrAdp.toggleImage(imageload);
        }
        catch(Exception err) {
            errmsg = "Fehler: "+err.getMessage();
            ErrorLog errorLog = new ErrorLog(ui.get());
            errorLog.add(errmsg);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        if(ui.get() == null)
            return;

        ProgressBar mProgress = ui.get().findViewById(R.id.userlist_progress);
        mProgress.setVisibility(View.INVISIBLE);

        if(errmsg == null) {
            userList.setAdapter(usrAdp);
        }
        else {
            Toast.makeText(ui.get(),errmsg,Toast.LENGTH_LONG).show();
        }
    }
}