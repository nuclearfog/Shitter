package org.nuclearfog.twidda.lists;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Status;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * custom status list implementation containing cursors
 *
 * @author nuclearfog
 */
public class Statuses extends LinkedList<Status> {

	private static final long serialVersionUID = 2077374641015738748L;

	/**
	 * max ID indicats that the list can't be extended by more items
	 */
	public static final long NO_ID = -1L;

	private long prevCursor;
	private long nextCursor;

	/**
	 * use status ID to determine minimum and maximum ID
	 */
	public Statuses() {
		this(0L, 0L);
	}

	/**
	 * create a copy of statuses
	 *
	 * @param statuses list to copy
	 */
	public Statuses(Statuses statuses) {
		super(statuses);
		this.prevCursor = statuses.getPreviousCursor();
		this.nextCursor = statuses.getNextCursor();
	}

	/**
	 * @param prevCursor previous cursor
	 * @param nextCursor next cursor
	 */
	public Statuses(long prevCursor, long nextCursor) {
		super();
		this.prevCursor = prevCursor;
		this.nextCursor = nextCursor;
	}

	/**
	 * @inheritDoc
	 */
	@Nullable
	@Override
	public Status get(int index) {
		return super.get(index);
	}

	/**
	 * get the previous cursor or the highest status ID
	 *
	 * @return minimum ID
	 */
	public long getPreviousCursor() {
		if (prevCursor != 0L) {
			return prevCursor;
		}
		for (Status item : this) {
			if (item != null) {
				return item.getId();
			}
		}
		return 0L;
	}

	/**
	 * get the next cursor if defined or lowest status ID
	 *
	 * @return maximum ID
	 */
	public long getNextCursor() {
		if (nextCursor != 0L) {
			return nextCursor;
		}
		Iterator<Status> iterator = descendingIterator();
		while (iterator.hasNext()) {
			Status item = iterator.next();
			if (item != null) {
				return item.getId();
			}
		}
		return 0L;
	}

	/**
	 * set maximum ID
	 *
	 * @param nextCursor new maximum ID
	 */
	public void setNextCursor(long nextCursor) {
		this.nextCursor = nextCursor;
	}

	/**
	 * add a sublist at specific position
	 *
	 * @param statuses sublist to add
	 * @param index    index where to insert the sublist
	 */
	public void addAll(int index, Statuses statuses) {
		if (isEmpty()) {
			prevCursor = statuses.getPreviousCursor();
			nextCursor = statuses.getNextCursor();
		} else if (index == 0) {
			prevCursor = statuses.getPreviousCursor();
		} else if (index == size() - 1) {
			nextCursor = statuses.getNextCursor();
		}
		super.addAll(index, statuses);
	}

	/**
	 * replace all items with new ones
	 *
	 * @param statuses new items to insert
	 */
	public void replaceAll(Statuses statuses) {
		clear();
		addAll(statuses);
		prevCursor = statuses.getPreviousCursor();
		nextCursor = statuses.getNextCursor();
	}
}