package org.nuclearfog.twidda.backend.helper.lists;

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
	 * ID used if the list can't be extended by more items
	 */
	public static final long NO_ID = -1L;

	private long minId = 0L;
	private long maxId = 0L;

	/**
	 * use status ID to determine minimum and maximum ID
	 */
	public Statuses() {
		super();
	}

	/**
	 * create a copy of statuses
	 *
	 * @param statuses list to copy
	 */
	public Statuses(Statuses statuses) {
		super(statuses);
		this.minId = statuses.getMinId();
		this.maxId = statuses.getMaxId();
	}

	/**
	 * @param minId minimum ID of the first item
	 * @param maxId maximum ID of the last item
	 */
	public Statuses(long minId, long maxId) {
		super();
		this.minId = minId;
		this.maxId = maxId;
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
	 * get the minimum ID of this list. If not set, use the first item's ID
	 *
	 * @return minimum ID
	 */
	public long getMinId() {
		if (minId != 0L) {
			return minId;
		}
		for (Status item : this) {
			if (item != null) {
				return item.getId();
			}
		}
		return 0L;
	}

	/**
	 * get the maximum ID of the list. If not set, use the last item's ID
	 *
	 * @return maximum ID
	 */
	public long getMaxId() {
		if (maxId != 0L) {
			return maxId;
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
	 * override maximum ID
	 *
	 * @param maxId new maximum ID
	 */
	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}

	/**
	 * add a sublist at the bottom of this list including next cursor
	 *
	 * @param statuses sublist to add
	 * @param index    index where to insert the sublist
	 */
	public void addAll(int index, Statuses statuses) {
		if (isEmpty()) {
			minId = statuses.getMinId();
			maxId = statuses.getMaxId();
		} else if (index == 0) {
			minId = statuses.getMinId();
		} else if (index == size() - 1) {
			maxId = statuses.getMaxId();
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
		minId = statuses.getMinId();
		maxId = statuses.getMaxId();
	}
}