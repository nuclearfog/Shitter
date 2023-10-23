package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.EditedStatus;

import java.util.LinkedList;

/**
 * @author nuclearfog
 */
public class StatusEditHistory extends LinkedList<EditedStatus> {

	private static final long serialVersionUID = 6241896565923670373L;

	/**
	 *
	 */
	public StatusEditHistory() {
	}

	/**
	 *
	 */
	public StatusEditHistory(StatusEditHistory items) {
		super(items);
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}
