package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.UserList;

import java.util.LinkedList;

/**
 * Container class for user lists containing cursor information
 *
 * @author nuclearfog
 */
public class UserLists extends LinkedList<UserList> {

	private static final long serialVersionUID = -5947008315897774115L;

	private long prevCursor, nextCursor;

	/**
	 *
	 */
	public UserLists() {
		this(0L, 0L);
	}

	/**
	 * @param prevCursor previous list cursor or 0 if list starts
	 * @param nextCursor next cursor or 0 if list ends
	 */
	public UserLists(long prevCursor, long nextCursor) {
		super();
		this.prevCursor = prevCursor;
		this.nextCursor = nextCursor;
	}

	/**
	 *
	 */
	public UserLists(UserLists userLists) {
		super.addAll(userLists);
		prevCursor = userLists.prevCursor;
		nextCursor = userLists.nextCursor;
	}


	@Nullable
	@Override
	public UserList get(int index) {
		return super.get(index);
	}

	/**
	 * get previous cursor of this list
	 *
	 * @return cursor
	 */
	public long getPreviousCursor() {
		return prevCursor;
	}

	/**
	 * get next cursor of this list
	 *
	 * @return cursor
	 */
	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * replace whole list including cursors
	 *
	 * @param userLists new list
	 */
	public void replaceAll(UserLists userLists) {
		super.clear();
		super.addAll(userLists);
		prevCursor = userLists.prevCursor;
		nextCursor = userLists.nextCursor;
	}

	/**
	 * remove an item from list
	 *
	 * @param id ID of the item
	 * @return index of the removed item
	 */
	public int removeItem(long id) {
		for (int index = 0; index < size(); index++) {
			UserList item = get(index);
			if (item != null && item.getId() == id) {
				remove(index);
				return index;
			}
		}
		return -1;
	}

	/**
	 * add a sublist at specific index
	 *
	 * @param list  sublist to add
	 * @param index index where to insert the sublist
	 */
	public void addAll(int index, UserLists list) {
		if (isEmpty()) {
			prevCursor = list.prevCursor;
			nextCursor = list.nextCursor;
		} else if (index == 0) {
			prevCursor = list.prevCursor;
		} else if (index == size() - 1) {
			nextCursor = list.nextCursor;
		}
		super.addAll(index, list);
	}


	@Override
	@NonNull
	public String toString() {
		int itemCount = 0;
		for (UserList item : this) {
			if (item != null) {
				itemCount++;
			}
		}
		return "item_count=" + itemCount + " previous=" + getPreviousCursor() + " next=" + getNextCursor();
	}
}