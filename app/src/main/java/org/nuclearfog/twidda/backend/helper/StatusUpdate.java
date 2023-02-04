package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to upload status information
 *
 * @author nuclearfog
 */
public class StatusUpdate {

	/**
	 * returned if attaching media failed
	 */
	public static final int MEDIA_ERROR = -1;

	/**
	 * indicates that there is no media attached
	 */
	public static final int MEDIA_NONE = 0;

	/**
	 * returned if image is attached
	 */
	public static final int MEDIA_IMAGE = 1;

	/**
	 * returned if video is attached
	 */
	public static final int MEDIA_VIDEO = 2;

	/**
	 * returned if an animated image is attached
	 */
	public static final int MEDIA_GIF = 3;


	private static final String MIME_GIF = "image/gif";
	private static final String MIME_IMAGE_ALL = "image/";
	private static final String MIME_VIDEO_ALL = "video/";

	private String text;
	private long replyId;
	private double longitude;
	private double latitude;

	private int mediaType = MEDIA_NONE;
	private List<Uri> mediaUris = new ArrayList<>(5);
	private MediaStatus[] mediaUpdates = {};
	private boolean hasLocation = false;
	private boolean mediaLimitReached = false;

	/**
	 * set ID of the replied status
	 *
	 * @param replyId status ID to reply
	 */
	public void setReplyId(long replyId) {
		this.replyId = replyId;
	}

	/**
	 * add status text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Add file uri and check if file is valid
	 *
	 * @param mediaUri uri to a local file
	 * @return number of media attached to this holder or -1 if file is invalid
	 */
	public int addMedia(Context context, Uri mediaUri) {
		String mime = context.getContentResolver().getType(mediaUri);
		Configuration configuration = GlobalSettings.getInstance(context).getLogin().getConfiguration();
		if (mime == null) {
			return MEDIA_ERROR;
		}
		// check if file is a 'gif' image
		else if (mime.equals(MIME_GIF)) {
			switch (mediaType) {
				case MEDIA_NONE:
					mediaType = MEDIA_GIF;

				case MEDIA_GIF:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getGifLimit()) {
							mediaLimitReached = true;
						}
						return MEDIA_GIF;
					}
					break;
			}

		}
		// check if file is an image
		else if (mime.startsWith(MIME_IMAGE_ALL)) {
			switch (mediaType) {
				case MEDIA_NONE:
					mediaType = MEDIA_IMAGE;

				case MEDIA_IMAGE:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getImageLimit()) {
							mediaLimitReached = true;
						}
						return MEDIA_IMAGE;
					}
					break;
			}
		}
		// check if file is a video
		else if (mime.startsWith(MIME_VIDEO_ALL)) {
			switch (mediaType) {
				case MEDIA_NONE:
					mediaType = MEDIA_VIDEO;

				case MEDIA_VIDEO:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getVideoLimit()) {
							mediaLimitReached = true;
						}
						return MEDIA_VIDEO;
					}
					break;
			}
		}
		return MEDIA_ERROR;
	}

	/**
	 * Add location to a status
	 *
	 * @param location location information
	 */
	public void setLocation(@NonNull Location location) {
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
		hasLocation = true;
	}

	/**
	 * get type of attached media
	 * currently there is only one type of media used at once
	 *
	 * @return media type {@link #MEDIA_NONE,#MEDIA_VIDEO,#MEDIA_IMAGE,#MEDIA_GIF}
	 */
	public int getMediaType() {
		return mediaType;
	}

	/**
	 * check if media limit is reached
	 *
	 * @return true if media limit is reached
	 */
	public boolean mediaLimitReached() {
		return mediaLimitReached;
	}

	/**
	 * get status text
	 *
	 * @return status text
	 */
	public String getText() {
		return text;
	}

	/**
	 * get ID of the replied status
	 *
	 * @return status ID
	 */
	public long getReplyId() {
		return replyId;
	}

	/**
	 * get information about media attached to the status
	 *
	 * @return list of media updates
	 */
	public MediaStatus[] getMediaUpdates() {
		return mediaUpdates;
	}

	/**
	 * get media links
	 *
	 * @return media uri array
	 */
	public Uri[] getMediaUris() {
		return mediaUris.toArray(new Uri[0]);
	}

	/**
	 * get longitude of the location
	 *
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * get latitude of the location
	 *
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * check if location informaton is attached
	 *
	 * @return true if location is attached
	 */
	public boolean hasLocation() {
		return hasLocation;
	}

	/**
	 * check if media information is attached
	 *
	 * @return true if media is attached
	 */
	public int mediaCount() {
		return mediaUris.size();
	}

	/**
	 * prepare media streams if media Uri is added
	 *
	 * @return true if success, false if an error occurs
	 */
	public boolean prepare(ContentResolver resolver) {
		if (mediaUris.isEmpty())
			return true;
		try {
			// open input streams
			mediaUpdates = new MediaStatus[mediaUris.size()];
			for (int i = 0; i < mediaUpdates.length; i++) {
				InputStream is = resolver.openInputStream(mediaUris.get(i));
				String mime = resolver.getType(mediaUris.get(i));
				// check if stream is valid
				if (is != null && mime != null && is.available() > 0) {
					mediaUpdates[i] = new MediaStatus(is, mime);
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * close all open streams
	 */
	public void close() {
		for (MediaStatus mediaUpdate : mediaUpdates) {
			mediaUpdate.close();
		}
	}

	@NonNull
	@Override
	public String toString() {
		if (replyId > 0)
			return "to=" + replyId + " tweet=\"" + text + "\"";
		return "tweet=\"" + text + "\"";
	}
}