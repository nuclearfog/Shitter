package org.nuclearfog.twidda.model;

import java.io.Serializable;

/**
 * An instance contains information about the host and limitations
 *
 * @author nuclearfog
 */
public interface Instance extends Serializable {

	/**
	 * get host title name
	 *
	 * @return title
	 */
	String getTitle();

	/**
	 * get host domain name
	 *
	 * @return domain name
	 */
	String getDomain();

	/**
	 * get API version
	 *
	 * @return version text
	 */
	String getVersion();

	/**
	 * get host description
	 *
	 * @return description of the host
	 */
	String getDescription();

	/**
	 * get time of the last update
	 *
	 * @return timestamp
	 */
	long getTimestamp();

	/**
	 * get limit of tag following
	 *
	 * @return max number of featured tags
	 */
	int getTagFollowLimit();

	/**
	 * get limit of characters supported in a status
	 *
	 * @return max character count
	 */
	int getStatusCharacterLimit();

	/**
	 * get limit of images that can attached to a status
	 *
	 * @return max image count
	 */
	int getImageLimit();

	/**
	 * get limit of videos that can attached to a status
	 *
	 * @return max video count
	 */
	int getVideoLimit();

	/**
	 * get limit of animated images (gif) that can attached to a status
	 *
	 * @return max gif count
	 */
	int getGifLimit();

	/**
	 * get limit of audio messages that can attached to a status
	 *
	 * @return max audio count
	 */
	int getAudioLimit();

	/**
	 * get a list of supported media MIME types
	 *
	 * @return array of supported MIME types
	 */
	String[] getSupportedFormats();

	/**
	 * get file size limit for images
	 *
	 * @return size limit in bytes
	 */
	int getImageSizeLimit();

	/**
	 * get file size limit for animated images
	 *
	 * @return size limit in bytes
	 */
	int getGifSizeLimit();

	/**
	 * get file size limit for videos
	 *
	 * @return size limit in bytes
	 */
	int getVideoSizeLimit();

	/**
	 * get file size limit for audio
	 *
	 * @return size limit in bytes
	 */
	int getAudioSizeLimit();

	/**
	 * get the poll options limit (how much options can be added to a poll)
	 *
	 * @return max options allowed
	 */
	int getPollOptionsLimit();

	/**
	 * get limit of poll option title length
	 *
	 * @return max character count
	 */
	int getPollOptionCharacterLimit();

	/**
	 * get minimum poll duration
	 *
	 * @return duration in seconds
	 */
	int getMinPollDuration();

	/**
	 * get max pol duration
	 *
	 * @return duration in seconds
	 */
	int getMaxPollDuration();

	/**
	 * get status translation support
	 *
	 * @return true if translation is supported
	 */
	boolean isTranslationSupported();
}