package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.util.List;

/**
 * Container class for user list information
 */
public class UserListHolder {

    private final List<TwitterUser> users;
    private final long prevCursor, nextCursor;

    public UserListHolder(List<TwitterUser> users, long prevCursor, long nextCursor) {
        this.users = users;
        this.prevCursor = prevCursor;
        this.nextCursor = nextCursor;
    }

    /**
     * check if list is linked to a previous list
     *
     * @return true if list is linked
     */
    public boolean hasPrevious() {
        return prevCursor != 0;
    }

    /**
     * check if list has a successor
     *
     * @return true if list has a successor
     */
    public boolean hasNext() {
        return nextCursor != 0;
    }

    /**
     * get next link to a list
     *
     * @return cursor
     */
    public long getNext() {
        return nextCursor;
    }

    /**
     * get size of the attached list
     *
     * @return size of the list
     */
    public int getSize() {
        return users.size();
    }

    /**
     * get attached list
     *
     * @return list
     */
    public List<TwitterUser> getUsers() {
        return users;
    }

    @Override
    @NonNull
    public String toString() {
        return "size=" + getSize() + " pre=" + prevCursor + " pos=" + nextCursor;
    }
}