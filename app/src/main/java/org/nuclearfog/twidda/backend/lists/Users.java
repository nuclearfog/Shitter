package org.nuclearfog.twidda.backend.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.User;

import java.util.LinkedList;

/**
 * custom twitter user list containing cursor information
 *
 * @author nuclearfog
 */
public class Users extends LinkedList<User> {

	public static final long serialVersionUID = -1108521824070076679L;

	private long prevCursor;
	private long nextCursor;

	/**
	 * creates an empty list with defined cursors
	 *
	 * @param prevCursor previous cursor of the list
	 * @param nextCursor next cursor of the list
	 */
	public Users(long prevCursor, long nextCursor) {
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
	public void replace(Users list) {
		super.clear();
		super.addAll(list);
		prevCursor = list.prevCursor;
		nextCursor = list.nextCursor;
	}

	/**
	 * set previous cursor
	 *
	 * @param prevCursor cursor value
	 */
	public void setPrevCursor(long prevCursor) {
		this.prevCursor = prevCursor;
	}

	/**
	 * set next cursor
	 *
	 * @param nextCursor cursor value
	 */
	public void setNextCursor(long nextCursor) {
		this.nextCursor = nextCursor;
	}

	/**
	 * add a sublist at the bottom of this list including next cursor
	 *
	 * @param list  new sublist
	 * @param index index of the sub list
	 */
	public void addAt(Users list, int index) {
		super.addAll(index, list);
		nextCursor = list.nextCursor;
	}

	@Override
	@NonNull
	public String toString() {
		return "size=" + size() + " pre=" + prevCursor + " pos=" + nextCursor;
	}
}