package org.nuclearfog.twidda.backend.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.model.Message;

import java.util.LinkedList;

/**
 * Message list container class containing cursor information
 *
 * @author nuclearfog
 */
public class MessageList extends LinkedList<Message> {

    private String prevCursor, nextCursor;

    /**
     * @param prevCursor cursor to a previous list
     * @param nextCursor cursor to a next list
     */
    public MessageList(String prevCursor, String nextCursor) {
        super();
        this.prevCursor = prevCursor;
        this.nextCursor = nextCursor;
    }

    @Override
    @Nullable
    public Message get(int index) {
        return super.get(index);
    }

    /**
     * replace old list with a new one
     *
     * @param list new list
     */
    public void replaceAll(MessageList list) {
        super.clear();
        super.addAll(list);
        prevCursor = list.prevCursor;
        nextCursor = list.nextCursor;
    }

    /**
     * add a new list to the bottom of this list
     *
     * @param list  new list
     * @param index Index of the sub list
     */
    public void addAt(MessageList list, int index) {
        super.addAll(index, list);
        nextCursor = list.nextCursor;
    }

    /**
     * remove message item matching with a given ID
     *
     * @param id message ID
     * @return index of the removed message or -1 if not found
     */
    public int removeItem(long id) {
        for (int index = 0; index < size(); index++) {
            Message item = get(index);
            if (item != null && item.getId() == id) {
                remove(index);
                return index;
            }
        }
        return -1;
    }

    /**
     * get next cursor string
     *
     * @return cursor string
     */
    public String getNextCursor() {
        return nextCursor;
    }

    /**
     * check if this list has a previous cursor
     *
     * @return true if list has a previous cursor
     */
    public boolean hasPrev() {
        return prevCursor != null;
    }

    /**
     * check if this list has a cursor to a next list
     *
     * @return true if list has a next cursor
     */
    public boolean hasNext() {
        return nextCursor != null;
    }


    @Override
    @NonNull
    public String toString() {
        return "size=" + size() + " pre=" + prevCursor + " pos=" + nextCursor;
    }
}