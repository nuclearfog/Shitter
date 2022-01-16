package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.ListEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.UserlistUpdate;
import org.nuclearfog.twidda.model.UserList;

import java.lang.ref.WeakReference;

/**
 * This class creates and updates user lists
 * Backend for {@link ListEditor}
 *
 * @author nuclearfog
 */
public class ListUpdater extends AsyncTask<Void, Void, UserList> {


    private WeakReference<ListEditor> callback;
    private TwitterException err;
    private Twitter twitter;

    private UserlistUpdate update;


    public ListUpdater(ListEditor activity, UserlistUpdate update) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        this.update = update;
    }


    @Override
    protected UserList doInBackground(Void... v) {
        try {
            if (update.exists())
                return twitter.updateUserlist(update);
            return twitter.createUserlist(update);
        } catch (TwitterException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(UserList result) {
        if (callback.get() != null) {
            if (result != null) {
                callback.get().onSuccess(result);
            } else {
                callback.get().onError(err);
            }
        }
    }
}