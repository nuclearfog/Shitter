package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.UserlistActivity;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.model.UserList;

import java.lang.ref.WeakReference;

/**
 * async task to load list information and take action to the list
 *
 * @author nuclearfog
 */
public class ListAction extends AsyncTask<Void, Void, UserList> {

    /**
     * load userlist information
     */
    public static final int LOAD = 1;

    /**
     * unfollow user list
     */
    public static final int FOLLOW = 2;

    /**
     * unfollow user list
     */
    public static final int UNFOLLOW = 3;

    /**
     * delete user list
     */
    public static final int DELETE = 4;


    private WeakReference<UserlistActivity> weakRef;
    private Twitter twitter;
    private TwitterException err;

    private long listId;
    private int action;

    /**
     * @param activity Callback to update list information
     * @param listId   ID of the list to process
     * @param action   what action should be performed
     */
    public ListAction(UserlistActivity activity, long listId, int action) {
        super();
        weakRef = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        this.listId = listId;
        this.action = action;
    }


    @Override
    protected UserList doInBackground(Void... v) {
        try {
            switch (action) {
                case LOAD:
                    return twitter.getUserlist1(listId);

                case FOLLOW:
                    return twitter.followUserlist(listId);

                case UNFOLLOW:
                    return twitter.unfollowUserlist(listId);

                case DELETE:
                    return twitter.deleteUserlist(listId);
            }
        } catch (TwitterException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserList userList) {
        UserlistActivity callback = this.weakRef.get();
        if (callback != null) {
            if (userList != null) {
                callback.onSuccess(userList, action);
            } else {
                callback.onFailure(err, listId);
            }
        }
    }
}