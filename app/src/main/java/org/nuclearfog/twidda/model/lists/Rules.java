package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Rule;

import java.util.ArrayList;

/**
 * @author nuclearfog
 */
public class Rules extends ArrayList<Rule> {

	private static final long serialVersionUID = -8984532893479237315L;

	/**
	 *
	 */
	public Rules() {
	}

	/**
	 *
	 */
	public Rules(int cap) {
		super(cap);
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}