package org.nuclearfog.twidda.backend.async;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;

import java.lang.ref.WeakReference;

/**
 * Backend async task to manage users on lists
 * Twitter users can be added and removed
 *
 * @author nuclearfog
 */
public class ListManager extends AsyncTask<Void, Void, Void> {

    /**
     * actions to be taken
     */
    public enum Action {
        /**
         * add user to list
         */
        ADD_USER,
        /**
         * remove user from list
         */
        DEL_USER
    }

    private TwitterException err;
    private Twitter twitter;
    private WeakReference<ListManagerCallback> weakRef;

    private long listId;
    private String username;
    private Action action;

    /**
     * @param c        activity context
     * @param listId   ID of the user list
     * @param action   what action should be performed
     * @param username name of the user to add or remove
     * @param callback callback to update information
     */
    public ListManager(Context c, long listId, Action action, String username, ListManagerCallback callback) {
        super();
        weakRef = new WeakReference<>(callback);
        twitter = Twitter.get(c);
        this.listId = listId;
        this.action = action;
        this.username = username;
    }


    @Override
    protected Void doInBackground(Void... v) {
        try {
            switch (action) {
                case ADD_USER:
                    twitter.addUserToUserlist(listId, username);
                    break;

                case DEL_USER:
                    twitter.removeUserFromUserlist(listId, username);
                    break;
            }
        } catch (TwitterException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        ListManagerCallback callback = weakRef.get();
        if (callback != null) {
            if (err == null) {
                callback.onSuccess(username);
            } else {
                callback.onFailure(err);
            }
        }
    }

    /**
     * Callback interface for Activities or fragments
     */
    public interface ListManagerCallback {

        /**
         * Called when AsyncTask finished successfully
         *
         * @param names the names of the users added or removed from list
         */
        void onSuccess(String names);

        /**
         * called when an error occurs
         *
         * @param err Engine exception thrown by backend
         */
        void onFailure(@Nullable ErrorHandler.TwitterError err);
    }
}