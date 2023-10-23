package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Hashtag;

import java.util.LinkedList;

/**
 * Hashtag list implementation with addtitional paging cursors
 *
 * @author nuclearfog
 */
public class Hashtags extends LinkedList<Hashtag> {

	private static final long serialVersionUID = 7646437787602696292L;

	private long prevCursor, nextCursor;

	/**
	 *
	 */
	public Hashtags() {
		this(0L, 0L);
	}

	/**
	 * @param prevCursor minimum ID of an item
	 * @param nextCursor maximum ID of an item
	 */
	public Hashtags(long prevCursor, long nextCursor) {
		super();
		this.nextCursor = nextCursor;
		this.prevCursor = prevCursor;
	}

	/**
	 * @param hashtags trend list to clone
	 */
	public Hashtags(Hashtags hashtags) {
		super(hashtags);
		prevCursor = hashtags.prevCursor;
		nextCursor = hashtags.nextCursor;
	}

	/**
	 * @inheritDoc
	 */
	@Nullable
	@Override
	public Hashtag get(int index) {
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
	 * @param hashtags sublist to add
	 * @param index    index where to insert the sublist
	 */
	public void addAll(int index, Hashtags hashtags) {
		if (isEmpty()) {
			prevCursor = hashtags.getPreviousCursor();
			nextCursor = hashtags.getNextCursor();
		} else if (index == 0) {
			prevCursor = hashtags.getPreviousCursor();
		} else if (index == size() - 1) {
			nextCursor = hashtags.getNextCursor();
		}
		super.addAll(index, hashtags);
	}

	/**
	 * replace all items with new ones
	 *
	 * @param hashtags new items to insert
	 */
	public void replaceAll(Hashtags hashtags) {
		clear();
		addAll(hashtags);
		prevCursor = hashtags.getPreviousCursor();
		nextCursor = hashtags.getNextCursor();
	}


	@Override
	@NonNull
	public String toString() {
		int itemCount = 0;
		for (Hashtag item : this) {
			if (item != null) {
				itemCount++;
			}
		}
		return "item_count=" + itemCount + " previous=" + getPreviousCursor() + " next=" + getNextCursor();
	}
}