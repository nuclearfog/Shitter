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
 * This background task loads profile information about a twitter user and take actions like following
 *
 * @see UserProfile
 */
public class ProfileLoader extends AsyncTask<ProfileLoader.Action, TwitterUser, UserRelation> {

    /**
     * actions to be taken
     */
    public enum Action {
        /**
         * Load profile information
         */
        LDR_PROFILE,
        /**
         * follow user
         */
        ACTION_FOLLOW,
        /**
         * un-follow user
         */
        ACTION_UNFOLLOW,
        /**
         * block user
         */
        ACTION_BLOCK,
        /**
         * un-block user
         */
        ACTION_UNBLOCK,
        /**
         * mute user
         */
        ACTION_MUTE,
        /**
         * un-mute user
         */
        ACTION_UNMUTE
    }

    @Nullable
    private EngineException twException;
    private WeakReference<UserProfile> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private long userId;
    private String screenName;

    /**
     * @param callback Callback to return the result
     * @param user     twitter user information
     */
    public ProfileLoader(UserProfile callback, TwitterUser user) {
        this(callback, user.getId(), user.getScreenname());
    }

    /**
     * @param callback   Callback to return the result
     * @param userId     ID of the twitter user
     * @param screenName username alternative to User ID
     */
    public ProfileLoader(UserProfile callback, long userId, String screenName) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
        this.userId = userId;
        this.screenName = screenName;
    }


    @Override
    protected UserRelation doInBackground(Action[] action) {
        try {
            switch (action[0]) {
                case LDR_PROFILE:
                    // load user information from database
                    TwitterUser user;
                    if (userId > 0) {
                        user = db.getUser(userId);
                        if (user != null) {
                            publishProgress(user);
                        }
                    }
                    // load user information from twitter
                    user = mTwitter.getUser(userId, screenName);
                    publishProgress(user);
                    db.storeUser(user);
                    // load user relations from twitter
                    UserRelation relation = mTwitter.getConnection(userId, screenName);
                    if (!relation.isHome()) {
                        boolean muteUser = relation.isBlocked() || relation.isMuted();
                        db.muteUser(userId, muteUser);
                    }
                    return relation;

                case ACTION_FOLLOW:
                    user = mTwitter.followUser(userId);
                    publishProgress(user);
                    break;

                case ACTION_UNFOLLOW:
                    user = mTwitter.unfollowUser(userId);
                    publishProgress(user);
                    break;

                case ACTION_BLOCK:
                    user = mTwitter.blockUser(userId);
                    publishProgress(user);
                    db.muteUser(userId, true);
                    break;

                case ACTION_UNBLOCK:
                    user = mTwitter.unblockUser(userId);
                    publishProgress(user);
                    db.muteUser(userId, false);
                    break;

                case ACTION_MUTE:
                    user = mTwitter.muteUser(userId);
                    publishProgress(user);
                    db.muteUser(userId, true);
                    break;

                case ACTION_UNMUTE:
                    user = mTwitter.unmuteUser(userId);
                    publishProgress(user);
                    db.muteUser(userId, false);
                    break;
            }
            return mTwitter.getConnection(userId, screenName);
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(TwitterUser[] users) {
        if (callback.get() != null) {
            callback.get().setUser(users[0]);
        }
    }


    @Override
    protected void onPostExecute(UserRelation properties) {
        if (callback.get() != null) {
            if (properties != null) {
                callback.get().onAction(properties);
            } else if (twException != null) {
                callback.get().onError(twException);
            }
        }
    }
}