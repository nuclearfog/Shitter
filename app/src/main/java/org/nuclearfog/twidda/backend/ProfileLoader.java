package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserProperties;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * task for loading user profile information and take actions
 * @see UserProfile
 */
public class ProfileLoader extends AsyncTask<Long, TwitterUser, UserProperties> {

    public enum Action {
        LDR_PROFILE,
        ACTION_FOLLOW,
        ACTION_BLOCK,
        ACTION_MUTE
    }

    private final Action action;
    private WeakReference<UserProfile> ui;
    private TwitterEngine mTwitter;
    private EngineException twException;
    private AppDatabase db;


    public ProfileLoader(@NonNull UserProfile context, Action action) {
        ui = new WeakReference<>(context);
        mTwitter = TwitterEngine.getInstance(context);
        db = new AppDatabase(context);
        this.action = action;
    }


    @Override
    protected UserProperties doInBackground(Long[] args) {
        UserProperties connection;
        TwitterUser user;
        long userId = args[0];
        try {
            switch (action) {
                case LDR_PROFILE:
                    user = db.getUser(userId);
                    if (user != null)
                        publishProgress(user);
                    user = mTwitter.getUser(userId);
                    publishProgress(user);
                    db.storeUser(user);

                    connection = mTwitter.getConnection(userId);
                    if (!connection.isHome())
                        if (connection.isBlocked() || connection.isMuted())
                            db.muteUser(userId, true);
                        else
                            db.muteUser(userId, false);
                    return connection;

                case ACTION_FOLLOW:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isFriend())
                        user = mTwitter.followUser(userId);
                    else
                        user = mTwitter.unfollowUser(userId);
                    publishProgress(user);
                    return mTwitter.getConnection(userId);

                case ACTION_BLOCK:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isBlocked()) {
                        user = mTwitter.blockUser(userId);
                        db.muteUser(userId, true);
                    } else {
                        user = mTwitter.unblockUser(userId);
                        db.muteUser(userId, false);
                    }
                    publishProgress(user);
                    return mTwitter.getConnection(userId);

                case ACTION_MUTE:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isMuted()) {
                        user = mTwitter.muteUser(userId);
                        db.muteUser(userId, true);
                    } else {
                        user = mTwitter.unmuteUser(userId);
                        db.muteUser(userId, false);
                    }
                    publishProgress(user);
                    return mTwitter.getConnection(userId);
            }
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(TwitterUser[] users) {
        final TwitterUser user = users[0];
        if (ui.get() != null && user != null) {
            ui.get().setUser(user);
        }
    }


    @Override
    protected void onPostExecute(@Nullable UserProperties properties) {
        if (ui.get() != null) {
            if (properties != null) {
                ui.get().setConnection(properties);
                ui.get().onAction(properties, action);
            } else if (twException != null) {
                ui.get().onError(twException);
            }
        }
    }
}