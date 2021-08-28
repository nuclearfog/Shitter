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
public class UserExcludeLoader extends AsyncTask<UserExcludeLoader.Mode, Void, UserExcludeLoader.Mode> {

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
    private String name;


    public UserExcludeLoader(UserExclude callback, String name) {
        super();
        mTwitter = TwitterEngine.getInstance(callback);
        excludeDatabase = ExcludeDatabase.getInstance(callback);
        this.callback = new WeakReference<>(callback);
        this.name = name;
    }


    @Override
    protected Mode doInBackground(Mode... mode) {
        try {
            switch (mode[0]) {
                case REFRESH:
                    List<Long> ids = mTwitter.getExcludedUserIDs();
                    excludeDatabase.setExcludeList(ids);
                    break;

                case MUTE_USER:
                    mTwitter.muteUser(name);
                    break;

                case BLOCK_USER:
                    mTwitter.blockUser(name);
                    break;
            }
        } catch (EngineException err) {
            this.err = err;
        }
        return mode[0];
    }


    @Override
    protected void onPostExecute(Mode mode) {
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