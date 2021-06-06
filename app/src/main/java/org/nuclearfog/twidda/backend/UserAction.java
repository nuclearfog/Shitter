package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;

import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.engine.TwitterEngine;
import org.nuclearfog.twidda.backend.model.Relation;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.AppDatabase;

import java.lang.ref.WeakReference;

/**
 * This background task loads profile information about a twitter user and take actions
 *
 * @author nuclearfog
 * @see UserProfile
 */
public class UserAction extends AsyncTask<UserAction.Action, User, Relation> {

    /**
     * actions to be taken
     */
    public enum Action {
        /**
         * Load profile information
         */
        PROFILE_lOAD,
        /**
         * load profile from database first
         */
        PROFILE_DB,
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

    private EngineException twException;
    private WeakReference<UserProfile> callback;
    private TwitterEngine mTwitter;
    private AppDatabase db;
    private long userId;

    /**
     * @param callback Callback to return the result
     * @param userId   ID of the twitter user
     */
    public UserAction(UserProfile callback, long userId) {
        super();
        this.callback = new WeakReference<>(callback);
        mTwitter = TwitterEngine.getInstance(callback);
        db = new AppDatabase(callback);
        this.userId = userId;
    }


    @Override
    protected Relation doInBackground(Action[] action) {
        try {
            switch (action[0]) {
                case PROFILE_DB:
                    // load user information from database
                    User user;
                    if (userId > 0) {
                        user = db.getUser(userId);
                        if (user != null) {
                            publishProgress(user);
                        }
                    }

                case PROFILE_lOAD:
                    // load user information from twitter
                    user = mTwitter.getUser(userId);
                    publishProgress(user);
                    db.storeUser(user);
                    // load user relations from twitter
                    Relation relation = mTwitter.getConnection(userId);
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
            return mTwitter.getConnection(userId);
        } catch (EngineException twException) {
            this.twException = twException;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(User[] users) {
        if (callback.get() != null) {
            callback.get().setUser(users[0]);
        }
    }


    @Override
    protected void onPostExecute(Relation relation) {
        if (callback.get() != null) {
            if (relation != null) {
                callback.get().onAction(relation);
            } else {
                callback.get().onError(twException);
            }
        }
    }
}