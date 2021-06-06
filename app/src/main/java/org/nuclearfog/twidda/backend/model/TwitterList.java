package org.nuclearfog.twidda.backend.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Class for Twitter list information
 *
 * @author nuclearfog
 */
public class TwitterList implements Serializable {

    private long id;
    private long createdAt;

    private User owner;
    private boolean isPrivate;
    private boolean isFollowing;
    private int memberCount;
    private int subscriberCnt;

    private String title = "";
    private String description = "";

    /**
     * @param list        Twitter4J List
     * @param homeId      ID of the authenticated user
     * @param isFollowing authenticated user is following list
     */
    public TwitterList(twitter4j.UserList list, long homeId, boolean isFollowing) {
        id = list.getId();
        createdAt = list.getCreatedAt().getTime();
        owner = new User(list.getUser(), homeId);
        isPrivate = !list.isPublic();
        memberCount = list.getMemberCount();
        subscriberCnt = list.getSubscriberCount();
        if (list.getName() != null)
            title = list.getName();
        if (list.getDescription() != null)
            description = list.getDescription();
        this.isFollowing = isFollowing;
    }

    public TwitterList(twitter4j.UserList list, long homeId) {
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
    public User getListOwner() {
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
        return owner.isCurrentUser();
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