package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.UserList;

/**
 * implementation of an user list
 *
 * @author nuclearfog
 */
class UserListV1 implements UserList {

    private long id;
    private long createdAt;

    private User owner;
    private boolean isPrivate;
    private boolean isFollowing;
    private int memberCount;
    private int subscriberCnt;

    private String title = "";
    private String description = "";


    UserListV1(twitter4j.UserList list, long homeId, boolean isFollowing) {
        id = list.getId();
        createdAt = list.getCreatedAt().getTime();
        owner = new UserV1(list.getUser(), homeId);
        isPrivate = !list.isPublic();
        memberCount = list.getMemberCount();
        subscriberCnt = list.getSubscriberCount();
        if (list.getName() != null)
            title = list.getName();
        if (list.getDescription() != null)
            description = list.getDescription();
        this.isFollowing = isFollowing;
    }


    UserListV1(twitter4j.UserList list, long homeId) {
        this(list, homeId, list.isFollowing());
    }

    /**
     * get List ID
     *
     * @return List ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * get date of Creation
     *
     * @return date long format
     */
    @Override
    public long getTimestamp() {
        return createdAt;
    }

    /**
     * get title of list
     *
     * @return title name
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * get description of list
     *
     * @return description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * get owner
     *
     * @return twitter user
     */
    @Override
    public User getListOwner() {
        return owner;
    }

    /**
     * get access information
     *
     * @return true is list is private
     */
    @Override
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * get follow status of the current user
     *
     * @return true if current user is following
     */
    @Override
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * get member count of the list
     *
     * @return member count
     */
    @Override
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * get subscriber count of the list
     *
     * @return subscriber count
     */
    @Override
    public int getSubscriberCount() {
        return subscriberCnt;
    }

    /**
     * check if list is owned by current user
     *
     * @return true if current user is owner
     */
    @Override
    public boolean isListOwner() {
        return owner.isCurrentUser();
    }


    @Override
    @NonNull
    public String toString() {
        return "title:" + title + " description:" + description;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof UserListV1)
            return ((UserListV1) o).id == id;
        return false;
    }
}