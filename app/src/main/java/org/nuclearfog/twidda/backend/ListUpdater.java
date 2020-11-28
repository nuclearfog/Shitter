package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.ListPopup;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.ListHolder;

import java.lang.ref.WeakReference;

/**
 * This class creates and updates user lists
 * Backend for {@link ListPopup}
 */
public class ListUpdater extends AsyncTask<ListHolder, Void, Boolean> {


    private EngineException err;
    private final TwitterEngine mTwitter;
    private final WeakReference<ListPopup> callback;


    public ListUpdater(ListPopup activity) {
        super();
        callback = new WeakReference<>(activity);
        mTwitter = TwitterEngine.getInstance(activity);
    }


    @Override
    protected Boolean doInBackground(ListHolder... listHolders) {
        try {
            ListHolder mList = listHolders[0];
            mTwitter.updateUserList(mList);
            return true;
        } catch (EngineException err) {
            this.err = err;
            return false;
        }
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