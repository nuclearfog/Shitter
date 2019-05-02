package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.ErrorHandler;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.TwitterException;

public class UserLoader extends AsyncTask<Long, Void, Boolean> {

    public enum Mode {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT
    }
    private Mode mode;
    private WeakReference<ViewGroup> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private UserAdapter adapter;
    private List<TwitterUser> users;


    public UserLoader(@NonNull ViewGroup root) {
        ui = new WeakReference<>(root);
        mTwitter = TwitterEngine.getInstance(root.getContext());
        RecyclerView list = (RecyclerView)root.getChildAt(0);
        adapter = (UserAdapter) list.getAdapter();
    }


    @Override
    protected void onPreExecute() {
        if(ui.get() == null)
            return;

        SwipeRefreshLayout reload = (SwipeRefreshLayout)ui.get();
        reload.setRefreshing(true);
    }


    @Override
    protected Boolean doInBackground(Long[] param) {
        try {
            switch(mode) {
                case FOLLOWS:
                    users = mTwitter.getFollower(param[0], -1);
                    break;

                case FRIENDS:
                    users = mTwitter.getFollowing(param[0], -1);
                    break;

                case RETWEET:
                    users = mTwitter.getRetweeter(param[0], -1);
                    break;

                case FAVORIT:
                    break;
            }
        } catch(TwitterException err) {
            this.err = err;
            return false;
        } catch(Exception err) {
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if(ui.get() == null)
            return;

        if(success) {
            adapter.setData(users);
            adapter.notifyDataSetChanged();
        } else {
            if(err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
        }
        SwipeRefreshLayout reload = (SwipeRefreshLayout) ui.get();
        reload.setRefreshing(false);
    }


    @Override
    protected void onCancelled() {
        if(ui.get() == null)
            return;

        SwipeRefreshLayout reload = (SwipeRefreshLayout) ui.get();
        reload.setRefreshing(false);
    }
}