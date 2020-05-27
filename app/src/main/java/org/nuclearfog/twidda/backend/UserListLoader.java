package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.fragment.UserFragment;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * download a list of user such as follower, following or searched users
 * @see UserFragment
 */
public class UserListLoader extends AsyncTask<Object, Void, List<TwitterUser>> {

    public enum Mode {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT,
        SEARCH,
        SUBSCRIBER,
        LIST
    }

    @Nullable
    private EngineException twException;
    private WeakReference<UserFragment> ui;
    private TwitterEngine mTwitter;
    private final Mode action;


    public UserListLoader(UserFragment fragment, Mode action) {
        ui = new WeakReference<>(fragment);
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        this.action = action;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<TwitterUser> doInBackground(Object[] param) {
        try {
            switch (action) {
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

                case SUBSCRIBER:
                    return mTwitter.getListFollower((long) param[0]);

                case LIST:
                    return mTwitter.getListMember((long) param[0]);

            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(List<TwitterUser> users) {
        if (ui.get() != null) {
            ui.get().setRefresh(false);
            if (users != null) {
                ui.get().setData(users);
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }
}