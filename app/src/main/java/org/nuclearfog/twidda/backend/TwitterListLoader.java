package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.UserListList;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.fragment.UserListFragment;

import java.lang.ref.WeakReference;


/**
 * Background task for downloading twitter lists created by a user
 *
 * @see UserListFragment
 */
public class TwitterListLoader extends AsyncTask<Long, Void, UserListList> {

    public static final long NO_CURSOR = -1;

    public enum Action {
        LOAD_USERLISTS,
        LOAD_MEMBERSHIPS,
        FOLLOW,
        DELETE
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<UserListFragment> callback;
    private final TwitterEngine mTwitter;
    private final Action action;

    private final long id;
    private final String ownerName;


    public TwitterListLoader(UserListFragment callback, Action action, long id, String ownerName) {
        super();
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.callback = new WeakReference<>(callback);
        this.action = action;
        this.ownerName = ownerName;
        this.id = id;
    }


    @Override
    protected UserListList doInBackground(Long[] param) {
        try {
            switch (action) {
                case LOAD_USERLISTS:
                    long cursor = param[0];
                    if (id > 0) {
                        return mTwitter.getUserList(id, cursor);
                    } else {
                        return mTwitter.getUserList(ownerName, cursor);
                    }

                case LOAD_MEMBERSHIPS:
                    cursor = param[0];
                    if (id > 0) {
                        return mTwitter.getUserListMemberships(id, "", cursor);
                    } else {
                        return mTwitter.getUserListMemberships(0, ownerName, cursor);
                    }

                case FOLLOW:
                    return new UserListList(mTwitter.followUserList(id));

                case DELETE:
                    return new UserListList(mTwitter.deleteUserList(id));
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserListList result) {
        if (callback.get() != null) {
            if (result != null) {
                switch (action) {
                    case LOAD_USERLISTS:
                    case LOAD_MEMBERSHIPS:
                        callback.get().setData(result);
                        break;

                    case FOLLOW:
                        TwitterList update = result.get(0);
                        callback.get().updateItem(update);
                        break;

                    case DELETE:
                        TwitterList remove = result.get(0);
                        callback.get().removeItem(remove);
                        break;
                }
            } else {
                callback.get().onError(twException);
            }
        }
    }
}