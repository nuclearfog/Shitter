package org.nuclearfog.twidda.backend.items;

import twitter4j.Relationship;

public class UserProperties {

    private final String targetScreenname;
    private final boolean isHome;
    private final boolean isFriend;
    private final boolean isFollower;
    private final boolean isBlocked;
    private final boolean isMuted;
    private final boolean canDm;


    public UserProperties(Relationship connect) {
        targetScreenname = '@' + connect.getTargetUserScreenName();
        isHome = connect.getSourceUserId() == connect.getTargetUserId();
        isFriend = connect.isSourceFollowingTarget();
        isFollower = connect.isTargetFollowingSource();
        isBlocked = connect.isSourceBlockingTarget();
        isMuted = connect.isSourceMutingTarget();
        canDm = connect.canSourceDm();
    }

    public String getTargetScreenname() {
        return targetScreenname;
    }

    public boolean isHome() {
        return isHome;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public boolean isFollower() {
        return isFollower;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public boolean canDm() {
        return canDm;
    }
}