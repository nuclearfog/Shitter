package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * represents a media attachment for a status/message
 *
 * @author nuclearfog
 */
public interface Media extends Serializable, Comparable<Media> {

	/**
	 * returned when the status doesn't contain any media
	 */
	int NONE = -1;

	/**
	 * returned when the status contains one or more images
	 */
	int PHOTO = 800;

	/**
	 * returned when the status contains a video
	 */
	int VIDEO = 801;

	/**
	 * returned when the status contains an animated gif
	 */
	int GIF = 802;

	/**
	 * @return media key
	 */
	String getKey();

	/**
	 * @return type of media e.g. video or image {@link #NONE,#GIF,#PHOTO,#VIDEO}
	 */
	int getMediaType();

	/**
	 * @return media url
	 */
	String getUrl();

	/**
	 * @return preview url
	 */
	String getPreviewUrl();
}