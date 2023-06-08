package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.net.Uri;

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
	private boolean sensitive = false;
	private boolean spoiler = false;
	private int visibility = Status.VISIBLE_PUBLIC;
	private String languageCode = "";
	private String text;

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
		languageCode = status.getLanguage();
		if (status.getPoll() != null) {
			poll = new PollUpdate(status.getPoll());
		}
		if (status.getLocation() != null) {
			location = new LocationUpdate(status.getLocation());
		}
		if (status.getMedia().length > 0) {
			for (Media media : status.getMedia()) {
				mediaStatuses.add(new MediaStatus(media));
			}
		}
		// fixme currently not possible to mix online and offline media files
		attachmentLimitReached = true;
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
	 * add status language
	 *
	 * @param languageCode ISO 639 language code or empty string to remove language
	 */
	public void addLanguage(@NonNull String languageCode) {
		this.languageCode = languageCode;
	}

	/**
	 * Add file uri and check if file is valid
	 *
	 * @param mediaUri uri to a local file
	 * @return number of media attached to this holder or {@link #MEDIA_ERROR} if an error occured
	 */
	public int addMedia(Context context, Uri mediaUri) {
		String mime = context.getContentResolver().getType(mediaUri);
		if (mime == null || instance == null || !supportedFormats.contains(mime) || attachmentLimitReached) {
			return MEDIA_ERROR;
		}
		try {
			MediaStatus mediaStatus = new MediaStatus(context, mediaUri, "");
			switch (mediaStatus.getMediaType()) {
				case MediaStatus.IMAGE:
					if (mediaStatuses.isEmpty() || mediaStatuses.get(0).getMediaType() == MediaStatus.IMAGE) {
						mediaStatuses.add(mediaStatus);
						if (mediaStatuses.size() == instance.getImageLimit())
							attachmentLimitReached = true;
						return MediaStatus.IMAGE;
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
	 * get status language
	 *
	 * @return ISO 639 language code
	 */
	@NonNull
	public String getLanguageCode() {
		return languageCode;
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
		Uri[] result = new Uri[mediaStatuses.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = Uri.parse(mediaStatuses.get(i).getPath());
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
		String[] keys = new String[mediaStatuses.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = mediaStatuses.get(i).getKey();
		}
		return keys;
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
		if (replyId != 0)
			return "to=" + replyId + " status=\"" + text + "\"";
		return "status=\"" + text + "\"";
	}
}