package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.TwitterList;

import java.lang.ref.WeakReference;

/**
 * async task to load list information and take action to the list
 *
 * @author nuclearfog
 */
public class ListAction extends AsyncTask<Long, Void, TwitterList> {

    /**
     * Actions to perform
     */
    public enum Action {
        /**
         * load userlist information
         */
        LOAD,
        /**
         * follow user list
         */
        FOLLOW,
        /**
         * unfollow user list
         */
        UNFOLLOW,
        /**
         * delete user list
         */
        DELETE,
    }

    private WeakReference<ListDetail> callback;
    private TwitterEngine mTwitter;
    private EngineException err;
    private Action action;

    private long missingListId;

    /**
     * @param callback Callback to update list information
     * @param action   what action should be performed
     */
    public ListAction(ListDetail callback, Action action) {
        super();
        mTwitter = TwitterEngine.getInstance(callback.getApplicationContext());
        this.callback = new WeakReference<>(callback);
        this.action = action;
    }


    @Override
    protected TwitterList doInBackground(Long... ids) {
        try {
            long listId = ids[0];
            switch (action) {
                case LOAD:
                    return mTwitter.loadUserList(listId);

                case FOLLOW:
                    return mTwitter.followUserList(listId, true);

                case UNFOLLOW:
                    return mTwitter.followUserList(listId, false);

                case DELETE:
                    return mTwitter.deleteUserList(listId);
            }
        } catch (EngineException err) {
            this.err = err;
            missingListId = ids[0];
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(TwitterList userList) {
        ListDetail callback = this.callback.get();
        if (callback != null) {
            if (userList != null) {
                callback.onSuccess(userList, action);
            } else {
                callback.onFailure(err, missingListId);
            }
        }
    }
}