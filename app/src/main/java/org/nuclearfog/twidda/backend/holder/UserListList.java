package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.items.TwitterList;

import java.util.LinkedList;

/**
 * Container class for user lists containing extra cursors for paging
 */
public class UserListList extends LinkedList<TwitterList> {

    private long prevCursor = 0;
    private long nextCursor = 0;

    /**
     * @param list single list item
     */
    public UserListList(TwitterList list) {
        super();
        add(list);
    }

    /**
     * @param prevCursor previous list cursor or 0 if list starts
     * @param nextCursor next cursor or 0 if list ends
     */
    public UserListList(long prevCursor, long nextCursor) {
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