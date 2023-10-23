package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import java.util.LinkedList;

/**
 * represents a list of domain url
 *
 * @author nuclearfog
 */
public class Domains extends LinkedList<String> {

	private static final long serialVersionUID = 7642308259992697427L;

	private long prevCursor, nextCursor;

	/**
	 *
	 */
	public Domains() {
		this(0L, 0L);
	}

	/**
	 * @param prevCursor cursor to the previous page
	 * @param nextCursor cursor to the next page
	 */
	public Domains(long prevCursor, long nextCursor) {
		super();
		this.prevCursor = prevCursor;
		this.nextCursor = nextCursor;
	}

	/**
	 * @param domains list to clone
	 */
	public Domains(Domains domains) {
		super(domains);
		prevCursor = domains.prevCursor;
		nextCursor = domains.nextCursor;
	}

	/**
	 * clone existing list
	 *
	 * @param domains list to clone
	 */
	public void replaceAll(Domains domains) {
		clear();
		addAll(domains);
		prevCursor = domains.prevCursor;
		nextCursor = domains.nextCursor;
	}

	/**
	 * add new items at specific index
	 *
	 * @param index index where to insert new items
	 * @param list  items to add
	 */
	public void addAll(int index, Domains list) {
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


	/**
	 * get previous cursor of this list
	 *
	 * @return cursor or 0L if not set
	 */
	public long getPreviousCursor() {
		return prevCursor;
	}

	/**
	 * get next cursor of this list
	 *
	 * @return cursor or 0L if not set
	 */
	public long getNextCursor() {
		return nextCursor;
	}


	@Override
	@NonNull
	public String toString() {
		int itemCount = 0;
		for (String item : this) {
			if (item != null) {
				itemCount++;
			}
		}
		return "item_count=" + itemCount + " previous=" + getPreviousCursor() + " next=" + getNextCursor();
	}
}