package org.nuclearfog.twidda.fragment.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.fragment.UserListFragment;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import twitter4j.TwitterException;

public class UserLoader extends AsyncTask<Object, Void, List<TwitterUser>> {

    public enum Mode {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT,
        SEARCH
    }

    private Mode mode;
    private WeakReference<UserListFragment> ui;
    private TwitterEngine mTwitter;
    private TwitterException err;
    private UserAdapter adapter;


    public UserLoader(UserListFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<TwitterUser> doInBackground(Object[] param) {
        try {
            switch (mode) {
                case FOLLOWS:
                    return mTwitter.getFollower((long) param[0]);

                case FRIENDS:
                    return mTwitter.getFollowing((long) param[0]);

                case RETWEET:
                    return mTwitter.getRetweeter((long) param[0]);

                case FAVORIT:
                    return new LinkedList<>();  // TODO not jet implemented in Twitter4J

                case SEARCH:
                    return mTwitter.searchUsers((String) param[0]);
            }
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable List<TwitterUser> users) {
        if (ui.get() != null) {
            if (users != null)
                adapter.replaceAll(users);
            else if (err != null)
                ErrorHandler.printError(ui.get().getContext(), err);
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }


    @Override
    protected void onCancelled(@Nullable List<TwitterUser> users) {
        if (ui.get() != null) {
            if (users != null)
                adapter.replaceAll(users);
            ui.get().setRefresh(false);
        }
    }
}