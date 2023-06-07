package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Trend;

import java.util.LinkedList;

/**
 * Trend list implementation with addtitional paging IDs
 *
 * @author nuclearfog
 */
public class Trends extends LinkedList<Trend> {

	private static final long serialVersionUID = 7646437787602696292L;

	private long prevCursor, nextCursor;

	/**
	 *
	 */
	public Trends() {
		this(0L, 0L);
	}

	/**
	 * @param prevCursor minimum ID of an item
	 * @param nextCursor maximum ID of an item
	 */
	public Trends(long prevCursor, long nextCursor) {
		super();
		this.nextCursor = nextCursor;
		this.prevCursor = prevCursor;
	}

	/**
	 * @param trends trend list to clone
	 */
	public Trends(Trends trends) {
		super(trends);
		prevCursor = trends.prevCursor;
		nextCursor = trends.nextCursor;
	}

	/**
	 * @inheritDoc
	 */
	@Nullable
	@Override
	public Trend get(int index) {
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
	 * @param trends sublist to add
	 * @param index  index where to insert the sublist
	 */
	public void addAll(int index, Trends trends) {
		if (isEmpty()) {
			prevCursor = trends.getPreviousCursor();
			nextCursor = trends.getNextCursor();
		} else if (index == 0) {
			prevCursor = trends.getPreviousCursor();
		} else if (index == size() - 1) {
			nextCursor = trends.getNextCursor();
		}
		super.addAll(index, trends);
	}

	/**
	 * replace all items with new ones
	 *
	 * @param trends new items to insert
	 */
	public void replaceAll(Trends trends) {
		clear();
		addAll(trends);
		prevCursor = trends.getPreviousCursor();
		nextCursor = trends.getNextCursor();
	}


	@Override
	@NonNull
	public String toString() {
		return "size=" + size() + " min_id=" + prevCursor + " max_id=" + nextCursor;
	}
}