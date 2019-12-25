package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import twitter4j.UserList;

/**
 * Class for Twitter list information
 */
public class TwitterList {

    private final long id;
    private final long createdAt;
    private final String shortName;
    private final String fullName;
    private final String description;

    private final TwitterUser owner;
    private final boolean isPrivate;
    private final boolean isFollowing;
    private final boolean enableFollow;
    private final int memberCount;
    private final int subscriberCnt;

    public TwitterList(UserList list, long homeId) {
        id = list.getId();
        shortName = list.getName();
        fullName = list.getFullName();
        createdAt = list.getCreatedAt().getTime();
        description = list.getDescription();
        owner = new TwitterUser(list.getUser());
        isFollowing = list.isFollowing();
        isPrivate = !list.isPublic();
        memberCount = list.getMemberCount();
        subscriberCnt = list.getSubscriberCount();
        enableFollow = homeId != owner.getId();
    }

    /**
     * get List ID
     *
     * @return List ID
     */
    public long getId() {
        return id;
    }

    /**
     * get date of Creation
     *
     * @return date long format
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * get short name of list
     *
     * @return name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * get full name of list
     *
     * @return name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * get description of list
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * get owner
     *
     * @return twitter user
     */
    public TwitterUser getListOwner() {
        return owner;
    }

    /**
     * get access information
     *
     * @return true is list is private
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * get follow status of the current user
     *
     * @return true if current user is following
     */
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * get member count of the list
     *
     * @return member count
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * get subscriber count of the list
     *
     * @return subscriber count
     */
    public int getSubscriberCount() {
        return subscriberCnt;
    }

    /**
     * return if current user can follow list
     *
     * @return true if user can follow list
     */
    public boolean enableFollow() {
        return enableFollow;
    }


    @Override
    @NonNull
    public String toString() {
        return shortName + " " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TwitterList)
            return ((TwitterList) o).id == id;
        return false;
    }
}