package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.fragment.UserListFragment;

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
    private EngineException twException;
    private final WeakReference<UserListFragment> callback;
    private final TwitterEngine mTwitter;
    private final Type listType;

    private final long userId;
    private final String ownerName;

    /**
     * @param callback  callback to update information
     * @param listType  type of list to load
     * @param userId    ID of the userlist
     * @param ownerName alternative if user id is not defined
     */
    public ListLoader(UserListFragment callback, Type listType, long userId, String ownerName) {
        super();
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.callback = new WeakReference<>(callback);
        this.listType = listType;
        this.userId = userId;
        this.ownerName = ownerName;
    }


    @Override
    protected UserLists doInBackground(Long[] param) {
        try {
            if (listType == Type.LOAD_USERLISTS) {
                return mTwitter.getUserList(userId, ownerName, param[0]);
            }
            if (listType == Type.LOAD_MEMBERSHIPS) {
                return mTwitter.getUserListMemberships(userId, ownerName, param[0]);
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
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