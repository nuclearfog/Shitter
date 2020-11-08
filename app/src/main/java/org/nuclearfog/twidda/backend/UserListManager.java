package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;

import java.lang.ref.WeakReference;

/**
 * Backend async task to manage users on user lists
 * Twitter users can be added and removed
 */
public class UserListManager extends AsyncTask<String, Void, String[]> {

    public enum Action {
        ADD_USER,
        DEL_USER
    }

    @Nullable
    private EngineException err;
    private final TwitterEngine mTwitter;
    private final WeakReference<ListManagerCallback> callback;

    private final long listId;
    private final Action mode;


    public UserListManager(long listId, Action mode, Context c, ListManagerCallback callback) {
        super();
        this.listId = listId;
        this.mode = mode;
        mTwitter = TwitterEngine.getInstance(c);
        this.callback = new WeakReference<>(callback);
    }


    @Override
    protected String[] doInBackground(String... strings) {
        try {
            switch (mode) {
                case ADD_USER:
                    mTwitter.addUserToList(listId, strings);
                    break;

                case DEL_USER:
                    mTwitter.delUserFromList(listId, strings[0]);
                    break;
            }

        } catch (EngineException err) {
            this.err = err;
            return null;
        }
        return strings;
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
        void onFailure(EngineException err);
    }
}