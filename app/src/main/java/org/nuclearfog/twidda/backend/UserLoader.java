package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.lists.UserList;
import org.nuclearfog.twidda.fragment.UserFragment;

import java.lang.ref.WeakReference;

/**
 * download a list of user such as follower, following or searched users
 *
 * @author nuclearfog
 * @see UserFragment
 */
public class UserLoader extends AsyncTask<Long, Void, UserList> {

    public static final long NO_CURSOR = -1;

    /**
     * actions to perform
     */
    public enum Type {
        /**
         * load follower list
         */
        FOLLOWS,
        /**
         * load following list
         */
        FRIENDS,
        /**
         * load users retweeting a tweet
         */
        RETWEET,
        /**
         * load users favoriting a tweet
         */
        FAVORIT,
        /**
         * search for users
         */
        SEARCH,
        /**
         * load users subscribing an userlist
         */
        SUBSCRIBER,
        /**
         * load members of an userlist
         */
        LISTMEMBER,
        NONE
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<UserFragment> callback;
    private final TwitterEngine mTwitter;

    private final Type type;
    private final String search;
    private final long id;


    public UserLoader(UserFragment callback, Type type, long id, String search) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.type = type;
        this.search = search;
        this.id = id;
    }


    @Override
    protected UserList doInBackground(Long[] param) {
        try {
            long cursor = param[0];
            switch (type) {
                case FOLLOWS:
                    return mTwitter.getFollower(id, cursor);

                case FRIENDS:
                    return mTwitter.getFollowing(id, cursor);

                case RETWEET:
                    return mTwitter.getRetweeter(id, cursor);

                case FAVORIT:
                    // TODO not implemented in Twitter4J
                    break;

                case SEARCH:
                    return mTwitter.searchUsers(search, cursor);

                case SUBSCRIBER:
                    return mTwitter.getListFollower(id, cursor);

                case LISTMEMBER:
                    return mTwitter.getListMember(id, cursor);

            }
        } catch (EngineException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserList users) {
        if (callback.get() != null) {
            if (users != null) {
                callback.get().setData(users);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}