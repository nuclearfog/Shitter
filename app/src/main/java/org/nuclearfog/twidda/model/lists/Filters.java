package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Filter;

import java.util.LinkedList;

/**
 * custom list implementation containing {@link Filter} elements
 *
 * @author nuclearfog
 */
public class Filters extends LinkedList<Filter> {

	private static final long serialVersionUID = -4302456429760810822L;

	/**
	 *
	 */
	public Filters() {
		super();
	}

	/**
	 *
	 */
	public Filters(Filters filters) {
		super(filters);
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}