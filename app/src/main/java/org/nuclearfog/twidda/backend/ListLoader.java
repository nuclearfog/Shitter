package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.fragments.UserListFragment;

import java.lang.ref.WeakReference;


/**
 * Background task for downloading twitter lists created by a user
 *
 * @author nuclearfog
 * @see UserListFragment
 */
public class ListLoader extends AsyncTask<Long, Void, UserLists> {

    public static final long NO_CURSOR = -1;

    /**
     * Type of list to be loaded
     */
    public enum Type {
        /**
         * load userlists of an user
         */
        LOAD_USERLISTS,
        /**
         * load userlists the specified user is on
         */
        LOAD_MEMBERSHIPS
    }

    @Nullable
    private TwitterException twException;
    private WeakReference<UserListFragment> callback;
    private Twitter twitter;
    private Type listType;
    private long userId;
    private String ownerName;

    /**
     * @param fragment  callback to update information
     * @param listType  type of list to load
     * @param userId    ID of the userlist
     * @param ownerName alternative if user id is not defined
     */
    public ListLoader(UserListFragment fragment, Type listType, long userId, String ownerName) {
        super();
        twitter = Twitter.get(fragment.getContext());
        callback = new WeakReference<>(fragment);
        this.listType = listType;
        this.userId = userId;
        this.ownerName = ownerName;
    }


    @Override
    protected UserLists doInBackground(Long[] param) {
        try {
            if (listType == Type.LOAD_USERLISTS) {
                return twitter.getUserListOwnerships(userId, ownerName);
            }
            if (listType == Type.LOAD_MEMBERSHIPS) {
                return twitter.getUserListMemberships(userId, ownerName, param[0]);
            }
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserLists result) {
        if (callback.get() != null) {
            if (result != null) {
                callback.get().setData(result);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}