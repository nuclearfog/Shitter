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
 * Task for editing profile information and updating images
 *
 * @see ProfileEditor
 */
public class ProfileUpdater extends AsyncTask<Void, Void, TwitterUser> {

    @Nullable
    private EngineException twException;
    private WeakReference<ProfileEditor> callback;
    private UserHolder userHolder;
    private TwitterEngine mTwitter;
    private AppDatabase db;


    public ProfileUpdater(ProfileEditor callback) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
    }


    public ProfileUpdater(ProfileEditor callback, UserHolder userHolder) {
        this(callback);
        this.userHolder = userHolder;
    }


    @Override
    protected void onPreExecute() {
        if (callback.get() != null) {
            callback.get().setLoading(true);
        }
    }


    @Override
    protected TwitterUser doInBackground(Void[] v) {
        try {
            if (userHolder == null) {
                return mTwitter.getCurrentUser();
            } else {
                TwitterUser user = mTwitter.updateProfile(userHolder);
                db.storeUser(user);
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable TwitterUser user) {
        if (callback.get() != null) {
            callback.get().setLoading(false);
            if (twException != null) {
                callback.get().setError(twException);
            } else if (user != null) {
                callback.get().setUser(user);
            } else if (userHolder != null) {
                callback.get().setSuccess();
            }
        }
    }
}