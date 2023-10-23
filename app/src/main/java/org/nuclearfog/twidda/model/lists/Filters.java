package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Filter;

import java.util.LinkedList;

/**
 * @author nuclearfog
 */
public class Filters extends LinkedList<Filter> {

	private static final long serialVersionUID = -4302456429760810822L;


	public Filters() {
		super();
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}