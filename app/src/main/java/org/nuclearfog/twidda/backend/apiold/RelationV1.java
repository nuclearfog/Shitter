package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Relation;

import twitter4j.Relationship;

/**
 * relationship implementation for Twitter4J
 *
 * @author nuclearfog
 */
class RelationV1 implements Relation {

    private boolean isHome;
    private boolean isFriend;
    private boolean isFollower;
    private boolean isBlocked;
    private boolean isMuted;
    private boolean canDm;


    RelationV1(Relationship connect) {
        isHome = connect.getSourceUserId() == connect.getTargetUserId();
        isFriend = connect.isSourceFollowingTarget();
        isFollower = connect.isTargetFollowingSource();
        isBlocked = connect.isSourceBlockingTarget();
        isMuted = connect.isSourceMutingTarget();
        canDm = connect.canSourceDm();
    }

    @Override
    public boolean isHome() {
        return isHome;
    }

    @Override
    public boolean isFollowing() {
        return isFriend;
    }

    @Override
    public boolean isFollower() {
        return isFollower;
    }

    @Override
    public boolean isBlocked() {
        return isBlocked;
    }

    @Override
    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public boolean canDm() {
        return canDm;
    }

    @NonNull
    @Override
    public String toString() {
        return "isFriend:" + isFriend + " isFollower:" + isFollower;
    }
}