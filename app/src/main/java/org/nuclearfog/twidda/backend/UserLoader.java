package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.lists.Users;
import org.nuclearfog.twidda.fragments.UserFragment;

import java.lang.ref.WeakReference;

/**
 * download a list of user such as follower, following or searched users
 *
 * @author nuclearfog
 * @see UserFragment
 */
public class UserLoader extends AsyncTask<Long, Void, Users> {

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
        /**
         *
         */
        BLOCK,
        /**
         *
         */
        MUTE,
        NONE
    }

    @Nullable
    private TwitterException twException;
    private final WeakReference<UserFragment> callback;
    private Twitter mTwitter;

    private final Type type;
    private final String search;
    private final long id;


    public UserLoader(UserFragment callback, Type type, long id, String search) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = Twitter.get(callback.getContext());
        this.type = type;
        this.search = search;
        this.id = id;
    }


    @Override
    protected Users doInBackground(Long[] param) {
        try {
            long cursor = param[0];
            switch (type) {
                case FOLLOWS:
                    return mTwitter.getFollower(id, cursor);

                case FRIENDS:
                    return mTwitter.getFollowing(id, cursor);

                case RETWEET:
                    return mTwitter.getRetweetingUsers(id);

                case FAVORIT:
                    return mTwitter.getLikingUsers(id);

                case SEARCH:
                    return mTwitter.searchUsers(search, cursor);

                case SUBSCRIBER:
                    return mTwitter.getListSubscriber(id, cursor);

                case LISTMEMBER:
                    return mTwitter.getListMember(id, cursor);

                case BLOCK:
                    return mTwitter.getBlockedUsers(cursor);

                case MUTE:
                    return mTwitter.getMutedUsers(cursor);

            }
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Users users) {
        if (callback.get() != null) {
            if (users != null) {
                callback.get().setData(users);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}