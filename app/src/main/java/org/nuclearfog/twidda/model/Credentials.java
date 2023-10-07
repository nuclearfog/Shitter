package org.nuclearfog.twidda.model;

import java.io.Serializable;

import org.nuclearfog.twidda.model.User.Field;

/**
 * represents credentials of the current user
 *
 * @author nuclearfog
 */
public interface Credentials extends Serializable {

	int DEFAULT = 0;

	int PUBLIC = 10;

	int PRIVATE = 11;

	int DIRECT = 12;

	int UNLISTED = 13;

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
	 * @return {@link #PUBLIC,#PRIVATE,#DIRECT,#UNLISTED}
	 */
	int getVisibility();

	/**
	 * get default sensitive setting for posts
	 */
	boolean isSensitive();

	/**
	 * get user fields
	 */
	Field[] getFields();
}