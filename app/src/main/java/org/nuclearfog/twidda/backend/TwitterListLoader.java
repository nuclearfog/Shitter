package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.fragment.ListFragment;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Background task for downloading twitter lists created by a user
 *
 * @see ListFragment
 */
public class TwitterListLoader extends AsyncTask<Object, TwitterList, List<TwitterList>> {

    public enum Action {
        LOAD,
        FOLLOW,
        DELETE
    }

    @Nullable
    private EngineException twException;
    private final WeakReference<ListFragment> callback;
    private final TwitterEngine mTwitter;
    private final Action action;


    public TwitterListLoader(ListFragment callback, Action action) {
        super();
        mTwitter = TwitterEngine.getInstance(callback.getContext());
        this.callback = new WeakReference<>(callback);
        this.action = action;
    }


    @Override
    protected List<TwitterList> doInBackground(Object[] param) {
        try {
            switch (action) {
                case LOAD:
                    if (param[0] instanceof Long) {
                        long ownerId = (long) param[0];
                        return mTwitter.getUserList(ownerId);
                    } else {
                        String ownerName = (String) param[0];
                        return mTwitter.getUserList(ownerName);
                    }

                case FOLLOW:
                    long listId = (long) param[0];
                    TwitterList result = mTwitter.followUserList(listId);
                    publishProgress(result);
                    break;

                case DELETE:
                    listId = (long) param[0];
                    TwitterList deletedList = mTwitter.deleteUserList(listId);
                    publishProgress(deletedList);
                    break;
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(TwitterList[] lists) {
        TwitterList list = lists[0];
        if (callback.get() != null) {
            if (action == Action.FOLLOW) {
                callback.get().updateItem(list);
            } else if (action == Action.DELETE) {
                callback.get().removeItem(list.getId());
            }
        }
    }


    @Override
    protected void onPostExecute(List<TwitterList> result) {
        if (callback.get() != null) {
            if (result != null) {
                callback.get().setData(result);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}