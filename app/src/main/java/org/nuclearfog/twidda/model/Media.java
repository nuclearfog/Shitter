package org.nuclearfog.twidda.model;

import androidx.annotation.Nullable;

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

	/**
	 * @return get media preview hash
	 */
	String getBlurHash();

	/**
	 * @return media information
	 */
	@Nullable
	Meta getMeta();


	@Override
	default int compareTo(Media o) {
		return String.CASE_INSENSITIVE_ORDER.compare(getKey(), o.getKey());
	}

	/**
	 * Media information
	 */
	interface Meta extends Serializable {

		/**
		 * get duration if video
		 *
		 * @return video duration in seconds
		 */
		double getDuration();

		/**
		 * @return image width of the thumbnail
		 */
		int getWidthPreview();

		/**
		 * @return image height of the thumbnail
		 */
		int getHeightPreview();

		/**
		 * @return image/video with
		 */
		int getWidth();

		/**
		 * @return image/video height
		 */
		int getHeight();

		/**
		 * get audio/video if any
		 *
		 * @return bitrate in kbit/s
		 */
		int getBitrate();

		/**
		 * get video framerate if any
		 *
		 * @return frame rate
		 */
		float getFrameRate();
	}
}