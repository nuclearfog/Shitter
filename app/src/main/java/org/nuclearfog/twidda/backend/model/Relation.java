package org.nuclearfog.twidda.backend.model;

import androidx.annotation.NonNull;

import twitter4j.Relationship;

/**
 * Holder for relationship information between the current user and another user
 *
 * @author nuclearfog
 */
public class Relation {

    private boolean isHome;
    private boolean isFriend;
    private boolean isFollower;
    private boolean isBlocked;
    private boolean isMuted;
    private boolean canDm;

    /**
     * Create relationship
     *
     * @param connect twitter4j relationship information
     */
    public Relation(Relationship connect) {
        isHome = connect.getSourceUserId() == connect.getTargetUserId();
        isFriend = connect.isSourceFollowingTarget();
        isFollower = connect.isTargetFollowingSource();
        isBlocked = connect.isSourceBlockingTarget();
        isMuted = connect.isSourceMutingTarget();
        canDm = connect.canSourceDm();
    }

    /**
     * return if target user is authenticating user
     *
     * @return true if target user is current user
     */
    public boolean isHome() {
        return isHome;
    }

    /**
     * return if target user is followed by current user
     *
     * @return true if target user is followed by current user
     */
    public boolean isFriend() {
        return isFriend;
    }

    /**
     * return if target user is following current user
     *
     * @return true if target user is following current user
     */
    public boolean isFollower() {
        return isFollower;
    }

    /**
     * return if current user is blocking target user
     *
     * @return true if current user is blocking target user
     */
    public boolean isBlocked() {
        return isBlocked;
    }

    /**
     * return if current user is muting target user
     *
     * @return true if current user is muting target user
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * return if target user can receive direct message
     *
     * @return true if target user can receive direct messages
     */
    public boolean canDm() {
        return canDm;
    }

    @NonNull
    @Override
    public String toString() {
        return ", isFriend=" + isFriend + ", isFollower=" + isFollower;
    }
}