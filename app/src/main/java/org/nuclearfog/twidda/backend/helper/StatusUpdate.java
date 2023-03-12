package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
	 * returned if an error occured while attaching item
	 */
	public static final int MEDIA_ERROR = -1;

	/**
	 * indicates that there is no attachment
	 */
	public static final int EMPTY = 0;

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

	/**
	 * returned if a poll is attached
	 */
	public static final int POLL = 4;

	/**
	 * status visibility
	 */
	public static final int PUBLIC = 10;
	public static final int UNLISTED = 11;
	public static final int DIRECT = 12;
	public static final int PRIVATE = 13;

	private static final String MIME_GIF = "image/gif";
	private static final String MIME_IMAGE_ALL = "image/";
	private static final String MIME_VIDEO_ALL = "video/";


	private long replyId;
	@Nullable
	private String text;
	@Nullable
	private PollUpdate poll;
	@Nullable
	private LocationUpdate location;

	private int attachment = EMPTY;
	private List<Uri> mediaUris = new ArrayList<>(5);
	private MediaStatus[] mediaUpdates = {};
	private boolean attachmentLimitReached = false;
	private boolean sensitive = false;
	private boolean spoiler = false;
	private int visibility = PUBLIC;


	/**
	 * set ID of the replied status
	 *
	 * @param replyId status ID to reply
	 */
	public void addReplyStatusId(long replyId) {
		this.replyId = replyId;
	}

	/**
	 * add status text
	 */
	public void addText(String text) {
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
			switch (attachment) {
				case EMPTY:
					attachment = MEDIA_GIF;

				case MEDIA_GIF:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getGifLimit()) {
							attachmentLimitReached = true;
						}
						return MEDIA_GIF;
					}
					break;
			}

		}
		// check if file is an image
		else if (mime.startsWith(MIME_IMAGE_ALL)) {
			switch (attachment) {
				case EMPTY:
					attachment = MEDIA_IMAGE;

				case MEDIA_IMAGE:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getImageLimit()) {
							attachmentLimitReached = true;
						}
						return MEDIA_IMAGE;
					}
					break;
			}
		}
		// check if file is a video
		else if (mime.startsWith(MIME_VIDEO_ALL)) {
			switch (attachment) {
				case EMPTY:
					attachment = MEDIA_VIDEO;

				case MEDIA_VIDEO:
					DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
					if (file != null && file.length() > 0) {
						mediaUris.add(mediaUri);
						if (mediaUris.size() == configuration.getVideoLimit()) {
							attachmentLimitReached = true;
						}
						return MEDIA_VIDEO;
					}
					break;
			}
		}
		return MEDIA_ERROR;
	}

	/**
	 * add poll to status
	 *
	 * @param poll poll information
	 */
	public void addPoll(@Nullable PollUpdate poll) {
		if (poll == null) {
			this.poll = null;
			attachment = EMPTY;
		} else if (attachment == EMPTY) {
			this.poll = poll;
			attachment = POLL;
			attachmentLimitReached = true;
		}
	}

	/**
	 * add location to status
	 *
	 * @param location location information
	 */
	public void addLocation(@NonNull Location location) {
		this.location = new LocationUpdate(location);
	}

	/**
	 * set status visibility
	 *
	 * @param visibility visibility states {@link #PUBLIC,#PRIVATE,#UNLISTED,#DIRECT}
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	/**
	 */
	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}

	/**
	 */
	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
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
	 * get status text
	 *
	 * @return status text
	 */
	@Nullable
	public String getText() {
		return text;
	}

	/**
	 * get type of attachment
	 * currently there is only one type of media used at once
	 *
	 * @return media type {@link #EMPTY,#MEDIA_VIDEO,#MEDIA_IMAGE,#MEDIA_GIF}
	 */
	public int getAttachmentType() {
		return attachment;
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
	 * get attached poll if any
	 *
	 * @return attached poll or null
	 */
	@Nullable
	public PollUpdate getPoll() {
		return poll;
	}

	/**
	 * get attached location update
	 *
	 * @return attached location or null
	 */
	@Nullable
	public LocationUpdate getLocation() {
		return location;
	}

	/**
	 * @return true if status content is sensitive
	 */
	public boolean isSensitive() {
		return sensitive;
	}

	/**
	 * @return true if status contains spoiler
	 */
	public boolean isSpoiler() {
		return spoiler;
	}

	/**
	 * get visibility states
	 *
	 * @return visibility states {@link #PUBLIC,#PRIVATE,#UNLISTED,#DIRECT}
	 */
	public int getVisibility() {
		return visibility;
	}

	/**
	 * check if media limit is reached
	 *
	 * @return true if media limit is reached
	 */
	public boolean mediaLimitReached() {
		return attachmentLimitReached;
	}

	/**
	 * check if media information is attached
	 *
	 * @return true if media is attached
	 */
	public boolean isEmpty() {
		return mediaUris.isEmpty() && location == null && poll == null && getText() == null;
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
		if (replyId != 0)
			return "to=" + replyId + " tweet=\"" + text + "\"";
		return "tweet=\"" + text + "\"";
	}
}