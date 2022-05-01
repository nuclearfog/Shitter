package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.ui.fragments.UserListFragment;

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
     * load userlists of an user
     */
    public static final int LOAD_USERLISTS = 1;

    /**
     * load userlists the specified user is on
     */
    public static final int LOAD_MEMBERSHIPS = 2;


    @Nullable
    private TwitterException twException;
    private WeakReference<UserListFragment> weakRef;
    private Twitter twitter;

    private int listType;
    private long userId;
    private String ownerName;

    /**
     * @param fragment  callback to update information
     * @param listType  type of list to load
     * @param userId    ID of the userlist
     * @param ownerName alternative if user id is not defined
     */
    public ListLoader(UserListFragment fragment, int listType, long userId, String ownerName) {
        super();
        twitter = Twitter.get(fragment.getContext());
        weakRef = new WeakReference<>(fragment);

        this.listType = listType;
        this.userId = userId;
        this.ownerName = ownerName;
    }


    @Override
    protected UserLists doInBackground(Long[] param) {
        try {
            switch(listType) {
                case LOAD_USERLISTS:
                    return twitter.getUserListOwnerships(userId, ownerName);

                case LOAD_MEMBERSHIPS:
                    return twitter.getUserListMemberships(userId, ownerName, param[0]);
            }
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserLists result) {
        UserListFragment fragment = weakRef.get();
        if (fragment != null) {
            if (result != null) {
                fragment.setData(result);
            } else {
                fragment.onError(twException);
            }
        }
    }
}