package org.nuclearfog.twidda.model.lists;

import org.nuclearfog.twidda.model.Notification;

import java.util.LinkedList;

/**
 * @author nuclearfog
 */
public class Notifications extends LinkedList<Notification> {

	private static final long serialVersionUID = 4522185068845443817L;

	/**
	 *
	 */
	public Notifications() {
		super();
	}

	/**
	 * @param notifications notification lsit to clone
	 */
	public Notifications(Notifications notifications) {
		super(notifications);
	}
}