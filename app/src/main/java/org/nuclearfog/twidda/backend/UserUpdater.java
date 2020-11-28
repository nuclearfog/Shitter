package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ProfileEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.UserHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<UserHolder, Void, TwitterUser> {

    @Nullable
    private EngineException twException;
    private WeakReference<ProfileEditor> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;


    public UserUpdater(ProfileEditor callback) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setLoading(true);
        }
    }


    @Override
    protected TwitterUser doInBackground(UserHolder[] holder) {
        try {
            if (holder.length == 0) {
                return mTwitter.getCurrentUser();
            } else {
                TwitterUser user = mTwitter.updateProfile(holder[0]);
                db.storeUser(user);
            }
        } catch (EngineException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable TwitterUser user) {
        ProfileEditor activity = callback.get();
        if (activity != null) {
            activity.setLoading(false);
            if (user != null) {
                activity.setUser(user);
            } else if (twException != null) {
                activity.setError(twException);
            } else {
                activity.setSuccess();
            }
        }
    }
}