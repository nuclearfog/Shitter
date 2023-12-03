package org.nuclearfog.twidda.model.lists;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Announcement;

import java.util.LinkedList;

/**
 * @author nuclearfog
 */
public class Announcements extends LinkedList<Announcement> {

	private static final long serialVersionUID = -4480120236663321113L;

	/**
	 *
	 */
	public Announcements() {
	}

	/**
	 *
	 */
	public Announcements(Announcements announcements) {
		super(announcements);
	}


	@NonNull
	@Override
	public String toString() {
		return "item_count=" + size();
	}
}