package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.ListDetail;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.model.UserList;

import java.lang.ref.WeakReference;

/**
 * async task to load list information and take action to the list
 *
 * @author nuclearfog
 */
public class ListAction extends AsyncTask<Long, Void, UserList> {

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
    private Twitter twitter;
    private TwitterException err;
    private Action action;

    private long missingListId;

    /**
     * @param activity Callback to update list information
     * @param action   what action should be performed
     */
    public ListAction(ListDetail activity, Action action) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        this.action = action;
    }


    @Override
    protected UserList doInBackground(Long... ids) {
        try {
            long listId = ids[0];
            switch (action) {
                case LOAD:
                    return twitter.getUserlist(listId);

                case FOLLOW:
                    return twitter.followUserlist(listId);

                case UNFOLLOW:
                    return twitter.unfollowUserlist(listId);

                case DELETE:
                    return twitter.destroyUserlist(listId);
            }
        } catch (TwitterException err) {
            this.err = err;
            missingListId = ids[0];
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserList userList) {
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