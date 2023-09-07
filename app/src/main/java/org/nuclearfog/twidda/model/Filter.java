package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Status filter interface used to filter status containing words from timelines
 *
 * @author nuclearfog
 */
public interface Filter extends Serializable {

	/**
	 * warn on filter match
	 */
	int ACTION_WARN = 1;

	/**
	 * hide status on filter match
	 */
	int ACTION_HIDE = 2;

	/**
	 * get filter ID
	 *
	 * @return filter ID
	 */
	long getId();

	/**
	 * get title of the filter
	 *
	 * @return title string
	 */
	String getTitle();

	/**
	 * get date time where the filter expires
	 *
	 * @return ISO 8601 Datetime or '0' if not defined
	 */
	long getExpirationTime();

	/**
	 * get an array of keywords to filter
	 *
	 * @return array of keywords
	 */
	Keyword[] getKeywords();

	/**
	 * get action to take when filtering a status
	 *
	 * @return action type {@link #ACTION_HIDE,#ACTION_WARN}
	 */
	int getAction();

	/**
	 * @return true to filter home timeline
	 */
	boolean filterHome();

	/**
	 * @return true to filter notification timeline
	 */
	boolean filterNotifications();

	/**
	 * @return true to filter public timelines
	 */
	boolean filterPublic();

	/**
	 * @return true to apply filter at threads
	 */
	boolean filterThreads();

	/**
	 * @return true to apply filter at user timelines
	 */
	boolean filterUserTimeline();

	/**
	 * Filter keyword used to filter statuses from timeline containing one of these words
	 */
	interface Keyword extends Serializable {

		/**
		 * get keyword ID
		 *
		 * @return ID
		 */
		long getId();

		/**
		 * get used keyword
		 *
		 * @return keyword text
		 */
		String getKeyword();

		/**
		 * @return true if single words should be interpreted as one word
		 */
		boolean isOneWord();
	}
}