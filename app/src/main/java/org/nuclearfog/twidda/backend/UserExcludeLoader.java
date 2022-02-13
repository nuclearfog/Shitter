package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.UserExclude;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.ExcludeDatabase;
import org.nuclearfog.twidda.model.User;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Backend of {@link UserExclude}
 * performs user mute or block actions and stores a list of IDs with blocked/muted users
 * This list is used to filter search results
 *
 * @author nuclearfog
 */
public class UserExcludeLoader extends AsyncTask<String, Void, Void> {

    public enum Mode {
        REFRESH,
        MUTE_USER,
        BLOCK_USER
    }

    @Nullable
    private TwitterException err;
    private WeakReference<UserExclude> weakRef;
    private ExcludeDatabase excludeDatabase;
    private AppDatabase appDatabase;
    private Twitter twitter;
    private Mode mode;


    public UserExcludeLoader(UserExclude activity, Mode mode) {
        super();
        twitter = Twitter.get(activity);
        appDatabase = new AppDatabase(activity);
        excludeDatabase = new ExcludeDatabase(activity);
        weakRef = new WeakReference<>(activity);
        this.mode = mode;
    }


    @Override
    protected Void doInBackground(String[] names) {
        try {
            if (mode == Mode.REFRESH) {
                List<Long> ids = twitter.getIdBlocklist();
                excludeDatabase.setExcludeList(ids);
            } else if (mode == Mode.MUTE_USER) {
                User user = twitter.muteUser(names[0]);
                appDatabase.storeUser(user);
            } else if (mode == Mode.BLOCK_USER) {
                User user = twitter.blockUser(names[0]);
                appDatabase.storeUser(user);
            }
        } catch (TwitterException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        UserExclude activity = weakRef.get();
        if (activity != null) {
            if (err == null) {
                activity.onSuccess(mode);
            } else {
                activity.onError(err);
            }
        }
    }
}