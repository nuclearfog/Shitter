package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.ProfileEditor;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<String, Void, User> {

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
    protected User doInBackground(String[] param) {
        try {
            String name = param[0];
            String link = param[1];
            String location = param[2];
            String bio = param[3];
            String profileImg = param[4];
            String bannerImg = param[5];
            User user = mTwitter.updateProfile(name, link, location, bio, profileImg, bannerImg);
            db.storeUser(user);
            return user;
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable User user) {
        ProfileEditor activity = callback.get();
        if (activity != null) {
            if (user != null) {
                activity.onSuccess(user);
            } else {
                activity.onError(twException);
            }
        }
    }
}