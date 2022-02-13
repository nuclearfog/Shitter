package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.ProfileEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.ProfileUpdate;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.User;

import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<Void, Void, User> {

    @Nullable
    private ErrorHandler.TwitterError exception;
    private WeakReference<ProfileEditor> weakRef;
    private Twitter twitter;
    private AppDatabase db;

    private ProfileUpdate profile;


    public UserUpdater(ProfileEditor activity, ProfileUpdate profile) {
        super();
        weakRef = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        db = new AppDatabase(activity);
        this.profile = profile;
    }


    @Override
    protected User doInBackground(Void[] v) {
        try {
            if (profile.getProfileImageStream() != null) {
                twitter.updateProfileImage(profile.getProfileImageStream());
            }
            if (profile.getBannerImageStream() != null) {
                twitter.updateBannerImage(profile.getBannerImageStream());
            }
            User user = twitter.updateProfile(profile);
            // save new user information
            db.storeUser(user);
            return user;
        } catch (TwitterException exception) {
            this.exception = exception;
        } finally {
            // close image streams
            profile.close();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable User user) {
        ProfileEditor activity = weakRef.get();
        if (activity != null) {
            if (user != null) {
                activity.onSuccess(user);
            } else {
                activity.onError(exception);
            }
        }
    }
}