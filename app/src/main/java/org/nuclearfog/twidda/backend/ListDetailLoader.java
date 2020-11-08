package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.ListDetail;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterList;

import java.lang.ref.WeakReference;

/**
 * async task to load list information and take action to the list
 */
public class ListDetailLoader extends AsyncTask<Long, Void, TwitterList> {

    public enum Action {
        LOAD,
        FOLLOW,
        UNFOLLOW,
        DELETE,
    }

    private WeakReference<ListDetail> callback;
    private TwitterEngine mTwitter;
    private EngineException err;
    private Action action;


    public ListDetailLoader(ListDetail callback, Action action) {
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
        }
        return null;
    }


    @Override
    protected void onPostExecute(TwitterList twitterList) {
        ListDetail callback = this.callback.get();
        if (callback != null) {
            if (twitterList != null) {
                callback.onSuccess(twitterList, action);
            } else {
                callback.onFailure(err);
            }
        }
    }
}