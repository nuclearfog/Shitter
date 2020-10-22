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

    public UserListList() {
        super();
    }

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
     * set next list cursor
     *
     * @param next
     */
    public void setNextCursor(long next) {
        nextCursor = next;
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
     * get prev link to a list
     *
     * @return cursor
     */
    public long getPrev() {
        return prevCursor;
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
     * replace whole list including cursors
     *
     * @param list new list
     */
    public void replace(UserListList list) {
        super.clear();
        super.addAll(list);
        prevCursor = list.getPrev();
        nextCursor = list.getNext();
    }

    /**
     * add a sublist at the bottom of this list including next cursor
     *
     * @param list  new sublist
     * @param index position where to insert at
     */
    public void addListAt(UserListList list, int index) {
        super.addAll(index, list);
        nextCursor = list.getNext();
    }

    @Override
    public void clear() {
        // resetting cursors
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