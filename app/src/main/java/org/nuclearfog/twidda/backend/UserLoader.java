package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.window.UserDetail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;

public class UserLoader extends AsyncTask<Long, Void, Boolean> {

    public enum Mode {
        FOLLOWING,
        FOLLOWERS,
        RETWEET,
        FAVORIT
    }
    private Mode mode;

    private WeakReference<UserDetail> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private UserAdapter usrAdp;
    private List<TwitterUser> user;

    public UserLoader(@NonNull UserDetail context, Mode mode) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        RecyclerView userList = context.findViewById(R.id.userlist);
        usrAdp = (UserAdapter) userList.getAdapter();
        user = new ArrayList<>();
        this.mode = mode;
    }


    @Override
    protected Boolean doInBackground(Long... data) {
        long id = data[0];
        long cursor = data[1];
        try {
            switch(mode) {
                case FOLLOWING:
                    user = mTwitter.getFollowing(id, cursor);
                    break;
                case FOLLOWERS:
                    user = mTwitter.getFollower(id, cursor);
                    break;
                case RETWEET:
                    user = mTwitter.getRetweeter(id, cursor);
                    break;
                case FAVORIT:
                    // TODO get User favoriting a tweet
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
            return false;
        } catch (Exception err) {
            if(err.getMessage() != null)
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
            if (err != null) {
                ErrorHandler.printError(ui.get(), err);
            }
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() == null) return;

        SwipeRefreshLayout refresh = ui.get().findViewById(R.id.user_refresh);
        refresh.setRefreshing(false);
    }
}