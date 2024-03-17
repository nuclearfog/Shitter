package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Instance;
import org.nuclearfog.twidda.model.Media;
import org.nuclearfog.twidda.model.Status;

import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * This class is used to upload status information
 *
 * @author nuclearfog
 */
public class StatusUpdate implements Serializable, Closeable {

	private static final long serialVersionUID = -5300983806882462557L;

	/**
	 * Return code used to indicate an error while adding media files
	 */
	public static final int MEDIA_ERROR = -2;

	// main attributes
	private long statusId = 0L;
	private long replyId = 0L;
	private String text;
	private StatusPreferenceUpdate statusPreferences = new StatusPreferenceUpdate();

	// attachment attributes
	private List<MediaStatus> mediaStatuses = new ArrayList<>(5);
	@Nullable
	private PollUpdate poll;
	@Nullable
	private LocationUpdate location;

	// helper attributes
	private TreeSet<String> supportedFormats = new TreeSet<>();
	private boolean attachmentLimitReached = false;
	@Nullable
	private Instance instance;

	/**
	 * close all open streams
	 */
	@Override
	public void close() {
		for (MediaStatus mediaUpdate : mediaStatuses) {
			if (mediaUpdate != null) {
				mediaUpdate.close();
			}
		}
	}

	/**
	 * add status information to edit
	 *
	 * @param status status to edit
	 */
	public void setStatusToEdit(Status status) {
		statusId = status.getId();
		replyId = status.getRepliedStatusId();
		text = status.getText();
		statusPreferences.setSensitive(status.isSensitive());
		statusPreferences.setSpoiler(status.isSpoiler());
		statusPreferences.setVisibility(status.getVisibility());
		statusPreferences.setLanguage(status.getLanguage());
		if (status.getPoll() != null) {
			poll = new PollUpdate(status.getPoll());
		}
		if (status.getLocation() != null) {
			location = new LocationUpdate(status.getLocation());
		}
		for (Media media : status.getMedia()) {
			mediaStatuses.add(new MediaStatus(media));
		}
		// fixme currently not possible to mix online and offline media files
		attachmentLimitReached = true;
	}

	/**
	 * add status information for a reply
	 *
	 * @param status status to reply
	 */
	public void setStatusToReply(Status status) {
		replyId = status.getId();
		text = status.getUserMentions();
		statusPreferences.setVisibility(status.getVisibility());
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
	 * @param mediaStatus meida to add
	 * @return number of media attached to this holder or {@link #MEDIA_ERROR} if an error occured
	 */
	public int addMedia(MediaStatus mediaStatus) {
		if (instance == null || mediaStatus.getMimeType() == null || !supportedFormats.contains(mediaStatus.getMimeType()) || attachmentLimitReached) {
			return MEDIA_ERROR;
		}
		try {
			switch (mediaStatus.getMediaType()) {
				case MediaStatus.PHOTO:
					if (mediaStatuses.isEmpty() || mediaStatuses.get(0).getMediaType() == MediaStatus.PHOTO) {
						mediaStatuses.add(mediaStatus);
						if (mediaStatuses.size() == instance.getImageLimit())
							attachmentLimitReached = true;
						return MediaStatus.PHOTO;
					}
					return MEDIA_ERROR;

				case MediaStatus.AUDIO:
					if (mediaStatuses.isEmpty() || mediaStatuses.get(0).getMediaType() == MediaStatus.AUDIO) {
						mediaStatuses.add(mediaStatus);
						if (mediaStatuses.size() == instance.getAudioLimit())
							attachmentLimitReached = true;
						return MediaStatus.AUDIO;
					}
					return MEDIA_ERROR;

				case MediaStatus.VIDEO:
					if (mediaStatuses.isEmpty() || mediaStatuses.get(0).getMediaType() == MediaStatus.VIDEO) {
						mediaStatuses.add(mediaStatus);
						if (mediaStatuses.size() == instance.getVideoLimit())
							attachmentLimitReached = true;
						return MediaStatus.VIDEO;
					}
					return MEDIA_ERROR;

				case MediaStatus.GIF:
					if (mediaStatuses.isEmpty() || mediaStatuses.get(0).getMediaType() == MediaStatus.GIF) {
						mediaStatuses.add(mediaStatus);
						if (mediaStatuses.size() == instance.getGifLimit())
							attachmentLimitReached = true;
						return MediaStatus.GIF;
					}
					return MEDIA_ERROR;
			}
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
		}
		return MEDIA_ERROR;
	}

	/**
	 * add poll to status
	 *
	 * @param poll poll information
	 */
	public void addPoll(PollUpdate poll) {
		if (mediaStatuses.isEmpty()) {
			this.poll = poll;
			attachmentLimitReached = poll != null;
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
	 *
	 */
	public void setStatusPreferences(StatusPreferenceUpdate statusPreferences) {
		this.statusPreferences = statusPreferences;
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
	 *
	 */
	public StatusPreferenceUpdate getStatusPreferences() {
		return statusPreferences;
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
	 * update existing media status
	 *
	 * @param mediaStatus media status to update
	 */
	public void updateMediaStatus(MediaStatus mediaStatus) {
		int index = mediaStatuses.indexOf(mediaStatus);
		if (index >= 0) {
			mediaStatuses.set(index, mediaStatus);
		}
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
		String[] keys = new String[mediaStatuses.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = mediaStatuses.get(i).getKey();
		}
		return keys;
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
		return mediaStatuses.isEmpty() && location == null && poll == null && (getText() == null || getText().trim().isEmpty());
	}

	/**
	 * prepare media streams if media Uri is added
	 *
	 * @return true if success, false if an error occurs
	 */
	public boolean prepare(ContentResolver resolver) {
		// skip preparation if there is no media attached
		if (mediaStatuses.isEmpty())
			return true;
		try {
			// open input streams
			for (MediaStatus mediaStatus : mediaStatuses) {
				if (mediaStatus.isLocal()) {
					boolean success = mediaStatus.openStream(resolver);
					if (!success) {
						return false;
					}
				}
			}
		} catch (Exception exception) {
			if (BuildConfig.DEBUG) {
				exception.printStackTrace();
			}
			return false;
		}
		return true;
	}


	@NonNull
	@Override
	public String toString() {
		if (replyId != 0L)
			return "to=" + replyId + " status=\"" + text + "\"";
		return "status=\"" + text + "\"";
	}
}