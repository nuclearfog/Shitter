package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.UserExclude;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.database.ExcludeDatabase;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Backend of {@link UserExclude}
 * performs user mute or block actions and exports block list to database
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
    private EngineException err;
    private WeakReference<UserExclude> callback;
    private ExcludeDatabase excludeDatabase;
    private TwitterEngine mTwitter;
    private Mode mode;


    public UserExcludeLoader(UserExclude callback, Mode mode) {
        super();
        mTwitter = TwitterEngine.getInstance(callback);
        excludeDatabase = new ExcludeDatabase(callback);
        this.callback = new WeakReference<>(callback);
        this.mode = mode;
    }


    @Override
    protected Void doInBackground(String[] names) {
        try {
            if (mode == Mode.REFRESH) {
                List<Long> ids = mTwitter.getExcludedUserIDs();
                excludeDatabase.setExcludeList(ids);
            } else if (mode == Mode.MUTE_USER) {
                mTwitter.muteUser(names[0]);
            } else if (mode == Mode.BLOCK_USER) {
                mTwitter.blockUser(names[0]);
            }
        } catch (EngineException err) {
            this.err = err;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        UserExclude activity = callback.get();
        if (activity != null) {
            if (err == null) {
                activity.onSuccess(mode);
            } else {
                activity.onError(err);
            }
        }
    }
}