package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;

import java.lang.ref.WeakReference;

public class UserListManager extends AsyncTask<String, Void, Boolean> {

    public enum Action {
        ADD_USER,
        DEL_USER
    }

    private final TwitterEngine mTwitter;
    private final WeakReference<ListManagerCallback> callback;

    private final long listId;
    private final Action mode;
    private EngineException err;


    public UserListManager(long listId, Action mode, Context c, ListManagerCallback callback) {
        super();
        this.listId = listId;
        this.mode = mode;
        mTwitter = TwitterEngine.getInstance(c);
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
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
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onFailure(err);
            }
        }
    }


    public interface ListManagerCallback {

        void onSuccess();

        void onFailure(EngineException err);
    }
}