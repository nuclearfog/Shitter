package org.nuclearfog.twidda.lists;

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

	private long minId = 0L;
	private long maxId = 0L;

	/**
	 *
	 */
	public Trends() {
		super();
	}

	/**
	 * @param minId minimum ID of an item
	 * @param maxId maximum ID of an item
	 */
	public Trends(long minId, long maxId) {
		super();
		this.maxId = maxId;
		this.minId = minId;
	}

	/**
	 * @param trends trend list to clone
	 */
	public Trends(Trends trends) {
		super(trends);
		minId = trends.minId;
		maxId = trends.maxId;
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
	public long getMinId() {
		return minId;
	}

	/**
	 * @param minId (internal) ID of the first item
	 */
	public void setMinId(long minId) {
		this.minId = minId;
	}

	/**
	 * @return (internal) ID of the last item
	 */
	public long getMaxId() {
		return maxId;
	}

	/**
	 * @param maxId  (internal) ID of the last item
	 */
	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}

	/**
	 * add a sublist at specific position
	 *
	 * @param trends   sublist to add
	 * @param index    index where to insert the sublist
	 */
	public void addAll(int index, Trends trends) {
		if (isEmpty()) {
			minId = trends.getMinId();
			maxId = trends.getMaxId();
		} else if (index == 0) {
			minId = trends.getMinId();
		} else if (index == size() - 1) {
			maxId = trends.getMaxId();
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
		minId = trends.getMinId();
		maxId = trends.getMaxId();
	}


	@Override
	@NonNull
	public String toString() {
		return "size=" + size() + " min_id=" + minId + " max_id=" + maxId;
	}
}