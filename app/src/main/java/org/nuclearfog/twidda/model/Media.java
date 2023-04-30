package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * represents a media attachment for a status/message
 *
 * @author nuclearfog
 */
public interface Media extends Serializable, Comparable<Media> {

	/**
	 * media is undefined
	 */
	int UNDEFINED = -1;

	/**
	 * media is a image
	 */
	int PHOTO = 800;

	/**
	 * media is a video
	 */
	int VIDEO = 801;

	/**
	 * media is an animated gif
	 */
	int GIF = 802;

	/**
	 * media is an audio
	 */
	int AUDIO = 803;

	/**
	 * @return media key
	 */
	String getKey();

	/**
	 * @return type of media e.g. video or image {@link #UNDEFINED ,#GIF,#PHOTO,#VIDEO}
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

	/**
	 * @return additional media description
	 */
	String getDescription();


	@Override
	default int compareTo(Media o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getKey(), o.getKey());
	}
}