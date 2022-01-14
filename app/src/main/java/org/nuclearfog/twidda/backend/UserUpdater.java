package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activities.ProfileEditor;
import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.api.holder.ProfileHolder;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.database.AppDatabase;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Background task for loading and editing profile information
 *
 * @author nuclearfog
 * @see ProfileEditor
 */
public class UserUpdater extends AsyncTask<Void, Void, User> {

    @Nullable
    private ErrorHandler.TwitterError twException;
    private WeakReference<ProfileEditor> callback;
    private Twitter twitter;
    private AppDatabase db;

    private ProfileHolder profile;


    public UserUpdater(ProfileEditor activity, ProfileHolder profile) {
        super();
        callback = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        db = new AppDatabase(activity);
        this.profile = profile;
    }


    @Override
    protected User doInBackground(Void[] v) {
        try {
            if (profile.getProfileImageStream() != null) {
                twitter.updateProfileImage(profile.getProfileImageStream());
                profile.getProfileImageStream().close();
            }
            if (profile.getBannerImageStream() != null) {
                twitter.updateProfileBanner(profile.getBannerImageStream());
                profile.getBannerImageStream().close();
            }
            User user = twitter.updateProfile(profile.getName(), profile.getUrl(), profile.getLocation(), profile.getDescription());
            db.storeUser(user);
            return user;
        } catch (TwitterException twException) {
            this.twException = twException;
        } catch (IOException e) {
            e.printStackTrace();
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