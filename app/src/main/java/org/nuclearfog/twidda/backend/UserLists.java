package org.nuclearfog.twidda.backend;

import android.content.Context;
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

import twitter4j.TwitterException;

public class UserLists extends AsyncTask <Long, Void, Boolean> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private UserRecycler usrAdp;
    private ErrorLog errorLog;
    private boolean imageLoad;
    private String errorMessage = "E: Userlist, ";
    private int retryAfter = 0;

    /**
     *@see UserDetail
     */
    public UserLists(Context context) {
        GlobalSettings settings = GlobalSettings.getInstance(context);
        imageLoad = settings.loadImages();
        errorLog = new ErrorLog(context);

        ui = new WeakReference<>((UserDetail)context);
        mTwitter = TwitterEngine.getInstance(context);
        RecyclerView userList = ui.get().findViewById(R.id.userlist);

        usrAdp = new UserRecycler(ui.get());
        userList.setAdapter(usrAdp);
    }


    @Override
    protected Boolean doInBackground(Long... data) {
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
            usrAdp.toggleImage(imageLoad);
            return true;
        }
        catch(TwitterException err) {
            int errCode = err.getErrorCode();
            if(errCode == 420) {
                retryAfter = err.getRetryAfter();
            } else {
                errorMessage += err.getMessage();
                errorLog.add(errorMessage);
            }
            return false;
        } catch(Exception err) {
            errorMessage += err.getMessage();
            errorLog.add(errorMessage);
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if(ui.get() == null)
            return;
        View mProgress = ui.get().findViewById(R.id.userlist_progress);
        mProgress.setVisibility(View.INVISIBLE);

        if(success) {
            usrAdp.notifyDataSetChanged();
        } else {
            if (retryAfter > 0)
                Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}