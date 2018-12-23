package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.window.UserDetail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class UserLists extends AsyncTask<Long, Void, Boolean> {

    public static final long FOLLOWING = 0L;
    public static final long FOLLOWERS = 1L;
    public static final long RETWEETER = 2L;
    public static final long FAVORISER = 3L;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private UserAdapter usrAdp;
    private List<TwitterUser> user;
    private String errorMessage = "E Userlist: ";
    private int returnCode = 0;


    public UserLists(UserDetail context) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        RecyclerView userList = context.findViewById(R.id.userlist);
        usrAdp = (UserAdapter) userList.getAdapter();
        user = new ArrayList<>();
    }


    @Override
    protected Boolean doInBackground(Long... data) {
        long id = data[0];
        long mode = data[1];
        long cursor = data[2];
        try {
            if (mode == FOLLOWING)
                user = mTwitter.getFollowing(id, cursor);
            else if (mode == FOLLOWERS)
                user = mTwitter.getFollower(id, cursor);
            else if (mode == RETWEETER)
                user = mTwitter.getRetweeter(id, cursor);
        } catch (TwitterException err) {
            returnCode = err.getErrorCode();
            if (returnCode > 0 && returnCode != 420) {
                errorMessage += err.getMessage();
            }
            return false;
        } catch (Exception err) {
            Log.e("User List", err.getMessage());
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (ui.get() == null) return;

        SwipeRefreshLayout refresh = ui.get().findViewById(R.id.user_refresh);
        refresh.setRefreshing(false);

        if (success) {
            usrAdp.setData(user);
            usrAdp.notifyDataSetChanged();
        } else {
            switch (returnCode) {
                case 420:
                    Toast.makeText(ui.get(), R.string.rate_limit_exceeded, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(ui.get(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onCancelled(Boolean b) {
        if (ui.get() == null) return;

        SwipeRefreshLayout refresh = ui.get().findViewById(R.id.user_refresh);
        refresh.setRefreshing(false);
    }
}