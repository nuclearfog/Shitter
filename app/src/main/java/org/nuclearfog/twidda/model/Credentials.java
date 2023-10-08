package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * represents credentials of the current user
 *
 * @author nuclearfog
 */
public interface Credentials extends Serializable {

	/**
	 * get user ID
	 *
	 * @return user ID
	 */
	long getId();

	/**
	 * get user display name
	 */
	String getUsername();

	/**
	 * get user description
	 */
	String getDescription();

	/**
	 * get default language for posting a status
	 *
	 * @return ISO 639 Part 1 two-letter language code or empty
	 */
	String getLanguage();

	/**
	 * get default visibility of the user's status
	 *
	 * @return {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_PRIVATE,Status#VISIBLE_DIRECT,Status#VISIBLE_UNLISTED}
	 */
	int getVisibility();

	/**
	 * get default sensitive setting for posts
	 */
	boolean isSensitive();

	/**
	 * check if account requires follow request
	 */
	boolean isLocked();
}