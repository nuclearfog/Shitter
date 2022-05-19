package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.backend.api.Twitter;
import org.nuclearfog.twidda.backend.api.TwitterException;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.database.FilterDatabase;
import org.nuclearfog.twidda.model.Relation;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.UserProfile;

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

    private ErrorHandler.TwitterError twException;
    private WeakReference<UserProfile> weakRef;
    private Twitter twitter;
    private FilterDatabase filterDatabase;
    private AppDatabase appDB;
    private long userId;

    /**
     * @param activity Callback to return the result
     * @param userId   ID of the twitter user
     */
    public UserAction(UserProfile activity, long userId) {
        super();
        this.weakRef = new WeakReference<>(activity);
        twitter = Twitter.get(activity);
        filterDatabase = new FilterDatabase(activity);
        appDB = new AppDatabase(activity);
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
                        user = appDB.getUser(userId);
                        if (user != null) {
                            publishProgress(user);
                        }
                    }

                case PROFILE_lOAD:
                    // load user information from twitter
                    user = twitter.showUser(userId);
                    publishProgress(user);
                    appDB.storeUser(user);
                    // load user relations from twitter
                    Relation relation = twitter.getRelationToUser(userId);
                    if (!relation.isHome()) {
                        boolean muteUser = relation.isBlocked() || relation.isMuted();
                        appDB.muteUser(userId, muteUser);
                    }
                    return relation;

                case ACTION_FOLLOW:
                    user = twitter.followUser(userId);
                    publishProgress(user);
                    break;

                case ACTION_UNFOLLOW:
                    user = twitter.unfollowUser(userId);
                    publishProgress(user);
                    break;

                case ACTION_BLOCK:
                    user = twitter.blockUser(userId);
                    publishProgress(user);
                    appDB.muteUser(userId, true);
                    break;

                case ACTION_UNBLOCK:
                    user = twitter.unblockUser(userId);
                    publishProgress(user);
                    // remove from exclude list only if user is not muted
                    relation = twitter.getRelationToUser(userId);
                    if (!relation.isMuted()) {
                        appDB.muteUser(userId, false);
                        filterDatabase.removeUser(userId);
                    }
                    return relation;

                case ACTION_MUTE:
                    user = twitter.muteUser(userId);
                    publishProgress(user);
                    appDB.muteUser(userId, true);
                    break;

                case ACTION_UNMUTE:
                    user = twitter.unmuteUser(userId);
                    publishProgress(user);
                    // remove from exclude list only if user is not blocked
                    relation = twitter.getRelationToUser(userId);
                    if (!relation.isBlocked()) {
                        appDB.muteUser(userId, false);
                        filterDatabase.removeUser(userId);
                    }
                    return relation;
            }
            return twitter.getRelationToUser(userId);
        } catch (TwitterException twException) {
            this.twException = twException;
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(User[] users) {
        UserProfile activity = weakRef.get();
        if (activity != null) {
            activity.setUser(users[0]);
        }
    }


    @Override
    protected void onPostExecute(Relation relation) {
        UserProfile activity = weakRef.get();
        if (activity != null) {
            if (relation != null) {
                activity.onAction(relation);
            } else {
                activity.onError(twException);
            }
        }
    }
}