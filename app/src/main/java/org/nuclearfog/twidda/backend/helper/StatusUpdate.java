package org.nuclearfog.twidda.backend.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to upload status information
 *
 * @author nuclearfog
 */
public class StatusUpdate implements Serializable {

	private static final long serialVersionUID = -5300983806882462557L;

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
	private static final int POLL = 4;

	private static final String MIME_GIF = "image/gif";
	private static final String MIME_IMAGE_ALL = "image/";
	private static final String MIME_VIDEO_ALL = "video/";

	// main attributes
	private long statusId = 0L;
	private long replyId = 0L;
	private boolean sensitive = false;
	private boolean spoiler = false;
	private int visibility = Status.VISIBLE_PUBLIC;
	private String text;

	// attachment attributes
	@Nullable
	private PollUpdate poll;
	@Nullable
	private LocationUpdate location;
	private int attachment = EMPTY;

	// helper attributes
	@Nullable
	private Instance instance;
	private List<String> previews = new ArrayList<>();
	private List<String> mediaKeys = new ArrayList<>();
	private List<MediaStatus> mediaStatuses = new ArrayList<>();

	private Set<String> supportedFormats = new TreeSet<>();
	private boolean attachmentLimitReached = false;

	/**
	 * set informations of an existing status to edit these
	 *
	 * @param status existing status
	 */
	public void setStatus(Status status) {
		statusId = status.getId();
		replyId = status.getRepliedStatusId();
		text = status.getText();
		sensitive = status.isSensitive();
		spoiler = status.isSpoiler();
		visibility = status.getVisibility();
		if (status.getPoll() != null) {
			poll = new PollUpdate(status.getPoll());
		}
		if (status.getLocation() != null) {
			location = new LocationUpdate(status.getLocation());
		}
	}

	/**
	 * set information  media files attached to an existing status
	 *
	 * @param status status contianing media
	 * @return attached media type {@link #EMPTY,#MEDIA_VIDEO,#MEDIA_IMAGE,#MEDIA_GIF}
	 */
	public int setMedia(Status status) {
		if (status.getMedia().length > 0) {
			for (Media media : status.getMedia()) {
				mediaKeys.add(media.getKey());
				previews.add(media.getUrl());
			}
			attachmentLimitReached = true;
			switch (status.getMedia()[0].getMediaType()) {
				case Media.GIF:
					attachment = MEDIA_GIF;
					break;

				case Media.PHOTO:
					attachment = MEDIA_IMAGE;
					break;

				case Media.VIDEO:
					attachment = MEDIA_VIDEO;
					break;
			}
		}
		return attachment;
	}

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
	 *
	 * @param text status text
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
		if (mime == null || instance == null || !supportedFormats.contains(mime)) {
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
						previews.add(mediaUri.toString());
						mediaStatuses.add(new MediaStatus(mediaUri.toString(), mime));
						if (mediaStatuses.size() == instance.getGifLimit()) {
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
						previews.add(mediaUri.toString());
						mediaStatuses.add(new MediaStatus(mediaUri.toString(), mime));
						if (mediaStatuses.size() == instance.getImageLimit()) {
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
						previews.add(mediaUri.toString());
						mediaStatuses.add(new MediaStatus(mediaUri.toString(), mime));
						if (mediaStatuses.size() == instance.getVideoLimit()) {
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
		this.location = new LocationUpdate(location.getLongitude(), location.getLatitude());
	}

	/**
	 * set status visibility
	 *
	 * @param visibility visibility states {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_DIRECT,Status#VISIBLE_PRIVATE,Status#VISIBLE_UNLISTED}
	 */
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	/**
	 * set spoiler flag
	 */
	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}

	/**
	 * set sensitive flag
	 */
	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	/**
	 * set instance imformation such as status limitations
	 *
	 * @param instance instance imformation
	 */
	public void setInstanceInformation(Instance instance) {
		supportedFormats.addAll(Arrays.asList(instance.getSupportedFormats()));
		this.instance = instance;
	}

	/**
	 * @return true to edit an existing status {@link #statusId} must be set
	 */
	public boolean statusExists() {
		return statusId != 0L;
	}

	/**
	 * get ID of an existing status to edit
	 *
	 * @return status ID or '0' to post a new status instead of edit
	 */
	public long getStatusId() {
		return statusId;
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
	public List<MediaStatus> getMediaStatuses() {
		return new ArrayList<>(mediaStatuses);
	}

	/**
	 * get media links
	 *
	 * @return media uri array
	 */
	public Uri[] getMediaUris() {
		Uri[] result = new Uri[previews.size()];
		for (int i = 0 ; i < result.length ; i++) {
			result[i] = Uri.parse(previews.get(i));
		}
		return result;
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
	 * get media keys (IDs) of online media
	 *
	 * @return media key
	 */
	public String[] getMediaKeys() {
		return mediaKeys.toArray(new String[0]);
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
	 * @return visibility states {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_DIRECT,Status#VISIBLE_PRIVATE,Status#VISIBLE_UNLISTED}
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
	 * get instance information
	 */
	@Nullable
	public Instance getInstance() {
		return instance;
	}

	/**
	 * check if media information is attached
	 *
	 * @return true if media is attached
	 */
	public boolean isEmpty() {
		return previews.isEmpty() && location == null && poll == null && getText() == null;
	}

	/**
	 * prepare media streams if media Uri is added
	 *
	 * @return true if success, false if an error occurs
	 */
	public boolean prepare(ContentResolver resolver) {
		if (previews.isEmpty())
			return true;
		try {
			// open input streams
			for (MediaStatus mediaStatus : mediaStatuses) {
				boolean success = mediaStatus.openStream(resolver);
				if (!success) {
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
		for (MediaStatus mediaUpdate : mediaStatuses) {
			if (mediaUpdate != null) {
				mediaUpdate.close();
			}
		}
	}

	@NonNull
	@Override
	public String toString() {
		if (replyId != 0)
			return "to=" + replyId + " status=\"" + text + "\"";
		return "status=\"" + text + "\"";
	}
}