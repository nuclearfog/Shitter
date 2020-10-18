package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.util.LinkedList;

/**
 * custom twitter user list with cursors included
 */
public class TwitterUserList extends LinkedList<TwitterUser> {

    private final long prevCursor, nextCursor;

    public TwitterUserList(long prevCursor, long nextCursor) {
        super();
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

    @Override
    @NonNull
    public String toString() {
        return "size=" + size() + " pre=" + prevCursor + " pos=" + nextCursor;
    }
}