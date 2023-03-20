package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * Represents a status translation
 *
 * @author nuclearfog
 */
public interface Translation extends Serializable {

	/**
	 * get the translation of a status
	 *
	 * @return translated text
	 */
	String getText();

	/**
	 * get source of translation
	 *
	 * @return source name
	 */
	String getSource();

	/**
	 * get original language of the translated text
	 *
	 * @return language name
	 */
	String getOriginalLanguage();
}