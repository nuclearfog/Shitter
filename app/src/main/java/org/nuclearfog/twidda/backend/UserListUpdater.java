package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ListPopup;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.ListHolder;

import java.lang.ref.WeakReference;

public class UserListUpdater extends AsyncTask<ListHolder, Void, Boolean> {

    private final WeakReference<ListPopup> callback;

    @Nullable
    private EngineException err;
    private final TwitterEngine mTwitter;


    public UserListUpdater(ListPopup activity) {
        super();
        callback = new WeakReference<>(activity);
        mTwitter = TwitterEngine.getInstance(activity);
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().startLoading();
        }
    }


    @Override
    protected Boolean doInBackground(ListHolder... listHolders) {
        try {
            mTwitter.updateUserList(listHolders[0]);
            return true;
        } catch (EngineException err) {
            this.err = err;
        }
        return false;
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback.get() != null) {
            if (success) {
                callback.get().onSuccess();
            } else {
                callback.get().onError(err);
            }
        }
    }
}