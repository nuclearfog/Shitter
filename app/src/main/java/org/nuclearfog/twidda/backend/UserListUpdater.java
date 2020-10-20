package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ListPopup;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.ListHolder;

import java.lang.ref.WeakReference;

/**
 * This class creates and updates user lists
 * Backend for {@link ListPopup}
 */
public class UserListUpdater extends AsyncTask<ListHolder, Void, ListHolder> {


    @Nullable
    private EngineException err;
    private final TwitterEngine mTwitter;
    private final WeakReference<ListPopup> callback;


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
    protected ListHolder doInBackground(ListHolder... listHolders) {
        try {
            ListHolder mList = listHolders[0];
            mTwitter.updateUserList(mList);
            return mList;
        } catch (EngineException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(ListHolder result) {
        if (callback.get() != null) {
            if (result != null) {
                callback.get().onSuccess(result);
            } else {
                callback.get().onError(err);
            }
        }
    }
}