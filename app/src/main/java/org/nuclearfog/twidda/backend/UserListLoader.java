package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.UserListHolder;
import org.nuclearfog.twidda.fragment.UserFragment;

import java.lang.ref.WeakReference;

/**
 * download a list of user such as follower, following or searched users
 *
 * @see UserFragment
 */
public class UserListLoader extends AsyncTask<Long, Void, UserListHolder> {

    public static final long NO_CURSOR = -1;

    public enum Action {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT,
        SEARCH,
        SUBSCRIBER,
        LIST,
        NONE
    }

    @Nullable
    private EngineException twException;
    private WeakReference<UserFragment> callback;
    private TwitterEngine mTwitter;

    private final Action action;
    private String search;
    private long id;


    public UserListLoader(UserFragment callback, Action action, long id, String search) {
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.action = action;
        this.search = search;
        this.id = id;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setRefresh(true);
        }
    }


    @Override
    protected UserListHolder doInBackground(Long[] param) {
        long cursor = param[0];
        try {
            switch (action) {
                case FOLLOWS:
                    return mTwitter.getFollower(id, cursor);

                case FRIENDS:
                    return mTwitter.getFollowing(id, cursor);

                case RETWEET:
                    return mTwitter.getRetweeter(id, cursor);

                case FAVORIT:
                    break;

                case SEARCH:
                    return mTwitter.searchUsers(search, cursor);

                case SUBSCRIBER:
                    return mTwitter.getListFollower(id, cursor);

                case LIST:
                    return mTwitter.getListMember(id, cursor);

            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserListHolder users) {
        if (callback.get() != null) {
            callback.get().setRefresh(false);
            if (users != null) {
                callback.get().setData(users);
            } else if (twException != null) {
                callback.get().onError(twException);
            }
        }
    }
}