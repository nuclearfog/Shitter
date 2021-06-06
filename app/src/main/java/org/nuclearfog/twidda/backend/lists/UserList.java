package org.nuclearfog.twidda.backend.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.model.User;

import java.util.LinkedList;

/**
 * custom twitter user list containing cursor information
 *
 * @author nuclearfog
 */
public class UserList extends LinkedList<User> {

    private long prevCursor = 0;
    private long nextCursor = 0;

    public UserList() {
        super();
    }

    /**
     * creates an empty list with defined cursors
     *
     * @param prevCursor previous cursor of the list
     * @param nextCursor next cursor of the list
     */
    public UserList(long prevCursor, long nextCursor) {
        super();
        this.prevCursor = prevCursor;
        this.nextCursor = nextCursor;
    }

    @Nullable
    @Override
    public User get(int index) {
        return super.get(index);
    }

    /**
     * remove user item from list matching screen name
     *
     * @param name screen name of the user
     * @return index of the user item or -1 if not found
     */
    public int removeItem(String name) {
        for (int index = 0; index < size(); index++) {
            User item = get(index);
            if (item != null && item.getScreenname().equals(name)) {
                remove(index);
                return index;
            }
        }
        return -1;
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
     * replace whole list including cursors
     *
     * @param list new list
     */
    public void replace(UserList list) {
        super.clear();
        super.addAll(list);
        prevCursor = list.prevCursor;
        nextCursor = list.nextCursor;
    }

    /**
     * add a sublist at the bottom of this list including next cursor
     *
     * @param list  new sublist
     * @param index index of the sub list
     */
    public void addAt(UserList list, int index) {
        super.addAll(index, list);
        nextCursor = list.nextCursor;
    }

    @Override
    @NonNull
    public String toString() {
        return "size=" + size() + " pre=" + prevCursor + " pos=" + nextCursor;
    }
}