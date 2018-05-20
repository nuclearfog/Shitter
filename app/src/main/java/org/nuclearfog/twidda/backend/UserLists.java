package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    private String errmsg;
    private boolean imageload;

    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        imageload = settings.getBoolean("image_load",true);

        ui = new WeakReference<>((UserDetail)context);
        mTwitter = TwitterEngine.getInstance(context);
        RecyclerView userList = ui.get().findViewById(R.id.userlist);

        usrAdp = new UserRecycler(ui.get());
        userList.setAdapter(usrAdp);
    }


    @Override
    protected Void doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            List<TwitterUser> user;
            if(mode == FOLLOWING) {
                user = mTwitter.getFollowing(id,cursor);
                usrAdp.setData(user);
            }
            else if(mode == FOLLOWERS) {
                user = mTwitter.getFollower(id,cursor);
                usrAdp.setData(user);
            }
            else if(mode == RETWEETER) {
                user = mTwitter.getRetweeter(id,cursor);
                usrAdp.setData(user);
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

        View mProgress = ui.get().findViewById(R.id.userlist_progress);
        mProgress.setVisibility(View.INVISIBLE);

        if(errmsg == null) {
            usrAdp.notifyDataSetChanged();
        }
        else {
            Toast.makeText(ui.get(),errmsg,Toast.LENGTH_LONG).show();
        }
    }
}