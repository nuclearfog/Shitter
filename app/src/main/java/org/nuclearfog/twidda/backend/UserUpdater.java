package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.ProfileEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
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
    private ErrorHandler.TwitterError twException;
    private WeakReference<ProfileEditor> callback;
    private Twitter twitter;
    private AppDatabase db;


    public UserUpdater(ProfileEditor activity) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        db = new AppDatabase(activity);
    }


    @Override
    protected User doInBackground(String[] param) {
        try {
            String name = param[0];
            String link = param[1];
            String location = param[2];
            String bio = param[3];
            String profileImg = param[4];
            String bannerImg = param[5];// fixme
            //User user = mTwitter.updateProfile(name, link, location, bio, profileImg, bannerImg);
            //db.storeUser(user);
            //return user;
        } /*catch (TwitterException twException) {
            this.twException = twException;
        }*/ catch (Exception err) {
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