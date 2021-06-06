package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;

import java.lang.ref.WeakReference;

/**
 * Backend async task to manage users on lists
 * Twitter users can be added and removed
 *
 * @author nuclearfog
 */
public class ListManager extends AsyncTask<String, Void, String[]> {

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

    private EngineException err;
    private final TwitterEngine mTwitter;
    private final WeakReference<ListManagerCallback> callback;

    private final long listId;
    private final Action action;

    /**
     * @param listId   ID of the user list
     * @param action   what action should be performed
     * @param c        activity context
     * @param callback callback to update information
     */
    public ListManager(long listId, Action action, Context c, ListManagerCallback callback) {
        super();
        this.listId = listId;
        this.action = action;
        mTwitter = TwitterEngine.getInstance(c);
        this.callback = new WeakReference<>(callback);
    }


    @Override
    protected String[] doInBackground(String... strings) {
        try {
            switch (action) {
                case ADD_USER:
                    mTwitter.addUserToList(listId, strings);
                    break;

                case DEL_USER:
                    mTwitter.delUserFromList(listId, strings[0]);
                    break;
            }
            return strings;
        } catch (EngineException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String[] names) {
        if (callback.get() != null) {
            if (names != null) {
                callback.get().onSuccess(names);
            } else {
                callback.get().onFailure(err);
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
        void onSuccess(String[] names);

        /**
         * called when an error occurs
         *
         * @param err Engine exception thrown by backend
         */
        void onFailure(@Nullable EngineException err);
    }
}