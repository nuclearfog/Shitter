package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.backend.items.User;

import java.util.LinkedList;

/**
 * custom twitter user list with cursors included
 *
 * @author nuclearfog
 */
public class TwitterUserList extends LinkedList<User> {

    private long prevCursor = 0;
    private long nextCursor = 0;

    public TwitterUserList() {
        super();
    }

    /**
     * creates an empty list with defined cursors
     *
     * @param prevCursor previous cursor of the list
     * @param nextCursor next cursor of the list
     */
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

    /**
     * get previous cursor
     *
     * @return previous cursor
     */
    public long getPrev() {
        return prevCursor;
    }

    /**
     * replace whole list including cursors
     *
     * @param list new list
     */
    public void replace(TwitterUserList list) {
        super.clear();
        super.addAll(list);
        prevCursor = list.getPrev();
        nextCursor = list.getNext();
    }

    /**
     * add a sublist at the bottom of this list including next cursor
     *
     * @param list new sublist
     */
    public void addListAt(TwitterUserList list, int index) {
        super.addAll(index, list);
        nextCursor = list.getNext();
    }

    @Override
    public void clear() {
        prevCursor = 0;
        nextCursor = 0;
        super.clear();
    }

    @Override
    @NonNull
    public String toString() {
        return "size=" + size() + " pre=" + prevCursor + " pos=" + nextCursor;
    }
}