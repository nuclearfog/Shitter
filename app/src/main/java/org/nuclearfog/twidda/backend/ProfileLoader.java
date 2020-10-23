package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.items.UserRelation;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * task for loading user profile information and take actions
 *
 * @see UserProfile
 */
public class ProfileLoader extends AsyncTask<Object, TwitterUser, UserRelation> {

    public enum Action {
        LDR_PROFILE,
        ACTION_FOLLOW,
        ACTION_BLOCK,
        ACTION_MUTE
    }

    @Nullable
    private EngineException twException;
    private WeakReference<UserProfile> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private final Action action;


    public ProfileLoader(UserProfile callback, Action action) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
        this.action = action;
    }


    @Override
    protected UserRelation doInBackground(Object[] args) {
        UserRelation connection;
        TwitterUser user;
        long userId = 0;
        String username = "";
        if (args[0] instanceof Long) {
            userId = (long) args[0];
        } else if (args[0] instanceof String) {
            username = (String) args[0];
        }
        try {
            switch (action) {
                case LDR_PROFILE:
                    if (userId > 0) { // search user by ID
                        user = db.getUser(userId);
                        if (user != null) {
                            publishProgress(user);
                        }
                        user = mTwitter.getUser(userId);
                    } else {    // Search user by name
                        user = mTwitter.getUser(username);
                    }
                    publishProgress(user);
                    db.storeUser(user);

                    if (userId > 0) {
                        connection = mTwitter.getConnection(userId);
                    } else {
                        connection = mTwitter.getConnection(username);
                    }
                    if (!connection.isHome()) {
                        db.muteUser(userId, connection.isBlocked() || connection.isMuted());
                    }
                    return connection;

                case ACTION_FOLLOW:
                    connection = mTwitter.getConnection(userId);
                    if (!connection.isFriend()) {
                        user = mTwitter.followUser(userId);
                    } else {
                        user = mTwitter.unfollowUser(userId);
                    }
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
        if (callback.get() != null && user != null) {
            callback.get().setUser(user);
        }
    }


    @Override
    protected void onPostExecute(UserRelation properties) {
        if (callback.get() != null) {
            if (properties != null) {
                callback.get().setConnection(properties);
                callback.get().onAction(properties, action);
            } else if (twException != null) {
                callback.get().onError(twException);
            }
        }
    }
}