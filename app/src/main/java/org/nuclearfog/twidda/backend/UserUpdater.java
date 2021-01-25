package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ProfileEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.holder.UserHolder;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<UserHolder, Void, User> {

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
    protected User doInBackground(UserHolder[] holder) {
        try {
            User user = mTwitter.updateProfile(holder[0]);
            db.storeUser(user);
            return user;
        } catch (EngineException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable User user) {
        ProfileEditor activity = callback.get();
        if (activity != null) {
            activity.setLoading(false);
            if (user != null) {
                activity.onSuccess(user);
            } else if (twException != null) {
                activity.onError(twException);
            }
        }
    }
}