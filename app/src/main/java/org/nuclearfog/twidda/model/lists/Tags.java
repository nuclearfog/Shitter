package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Tag;

import java.util.LinkedList;

/**
 * Custom {@link Tag} list with addtitional paging cursors
 *
 * @author nuclearfog
 */
public class Tags extends LinkedList<Tag> {

	private static final long serialVersionUID = 7646437787602696292L;

	private long prevCursor, nextCursor;

	/**
	 *
	 */
	public Tags() {
		this(0L, 0L);
	}

	/**
	 * @param prevCursor minimum ID of an item
	 * @param nextCursor maximum ID of an item
	 */
	public Tags(long prevCursor, long nextCursor) {
		super();
		this.nextCursor = nextCursor;
		this.prevCursor = prevCursor;
	}

	/**
	 * @param tags tag list to clone
	 */
	public Tags(Tags tags) {
		super(tags);
		prevCursor = tags.prevCursor;
		nextCursor = tags.nextCursor;
	}

	/**
	 * @inheritDoc
	 */
	@Nullable
	@Override
	public Tag get(int index) {
		return super.get(index);
	}

	/**
	 * @return (internal) ID of the first item
	 */
	public long getPreviousCursor() {
		return prevCursor;
	}

	/**
	 * @return (internal) ID of the last item
	 */
	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * add a sublist at specific position
	 *
	 * @param tags  sublist to add
	 * @param index index where to insert the sublist
	 */
	public void addAll(int index, Tags tags) {
		if (isEmpty()) {
			prevCursor = tags.getPreviousCursor();
			nextCursor = tags.getNextCursor();
		} else if (index == 0) {
			prevCursor = tags.getPreviousCursor();
		} else if (index == size() - 1) {
			nextCursor = tags.getNextCursor();
		}
		super.addAll(index, tags);
	}

	/**
	 * replace all items with new ones
	 *
	 * @param tags new items to insert
	 */
	public void replaceAll(Tags tags) {
		clear();
		addAll(tags);
		prevCursor = tags.getPreviousCursor();
		nextCursor = tags.getNextCursor();
	}


	@Override
	@NonNull
	public String toString() {
		int itemCount = 0;
		for (Tag item : this) {
			if (item != null) {
				itemCount++;
			}
		}
		return "item_count=" + itemCount + " previous=" + getPreviousCursor() + " next=" + getNextCursor();
	}
}