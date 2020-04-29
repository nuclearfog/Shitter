package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import twitter4j.UserList;

/**
 * Class for Twitter list information
 */
public class TwitterList {

    private final long id;
    private final long createdAt;
    private final String title;
    private final String description;

    private final TwitterUser owner;
    private final boolean isPrivate;
    private final boolean isFollowing;
    private final boolean isOwner;
    private final int memberCount;
    private final int subscriberCnt;

    public TwitterList(UserList list, long homeId, boolean isFollowing) {
        String description = list.getDescription();
        String title = list.getName();
        id = list.getId();
        createdAt = list.getCreatedAt().getTime();
        owner = new TwitterUser(list.getUser());
        isPrivate = !list.isPublic();
        memberCount = list.getMemberCount();
        subscriberCnt = list.getSubscriberCount();
        isOwner = homeId != owner.getId();
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.isFollowing = isFollowing;
    }

    public TwitterList(UserList list, long homeId) {
        this(list, homeId, list.isFollowing());
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
     * get title of list
     *
     * @return title name
     */
    public String getTitle() {
        return title;
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
     * check if list is owned by current user
     *
     * @return true if current user is owner
     */
    public boolean isListOwner() {
        return isOwner;
    }


    @Override
    @NonNull
    public String toString() {
        return title + " " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TwitterList)
            return ((TwitterList) o).id == id;
        return false;
    }
}