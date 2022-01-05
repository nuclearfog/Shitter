package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * interface of an user list
 *
 * @author nuclearfog
 */
public interface UserList extends Serializable {

    /**
     * @return ID of the user list
     */
    long getId();

    /**
     * @return date of creation
     */
    long getCreatedAt();

    /**
     * @return title of the list
     */
    String getTitle();

    /**
     * @return description of the list
     */
    String getDescription();

    /**
     * @return owner of the list
     */
    User getListOwner();

    /**
     * @return true if list is private
     */
    boolean isPrivate();

    /**
     * @return true if current user is following the list
     */
    boolean isFollowing();

    /**
     * @return list member count
     */
    int getMemberCount();

    /**
     * @return list subscriber count
     */
    int getSubscriberCount();

    /**
     * @return true if current user owns the list
     */
    boolean isListOwner();
}