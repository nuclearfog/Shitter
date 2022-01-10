package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activities.ListEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.holder.ListHolder;
import org.nuclearfog.twidda.model.UserList;

import java.lang.ref.WeakReference;

/**
 * This class creates and updates user lists
 * Backend for {@link ListEditor}
 *
 * @author nuclearfog
 */
public class ListUpdater extends AsyncTask<ListHolder, Void, UserList> {


    private TwitterException err;
    private Twitter twitter;
    private WeakReference<ListEditor> callback;


    public ListUpdater(ListEditor activity) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
    }


    @Override
    protected UserList doInBackground(ListHolder... listHolders) {
        try {
            ListHolder list = listHolders[0];
            if (list.exists())
                return twitter.updateUserlist(list.getId(), list.isPublic(), list.getTitle(), list.getDescription());
            return twitter.createUserlist(list.isPublic(), list.getTitle(), list.getDescription());
        } catch (TwitterException err) {
            this.err = err;
        } catch (Exception err) {
            err.printStackTrace();
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