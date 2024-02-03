package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Credentials;
import org.nuclearfog.twidda.model.User;

import java.io.Closeable;
import java.io.Serializable;

/**
 * This class is used to upload profile information
 *
 * @author nuclearfog
 */
public class UserUpdate implements Serializable, Closeable {

	private static final long serialVersionUID = -7555621393621077213L;

	private MediaStatus profileImage, bannerImage;

	private String name = "";
	private String description = "";
	private String location = "";
	private String userUrl = "";
	private String profileImageUrl = "";
	private String bannerImageUrl = "";
	private boolean privacy = false;

	private StatusPreferenceUpdate statusPref;

	/**
	 * close all image streams
	 */
	@Override
	public void close() {
		if (profileImage != null) {
			profileImage.close();
		}
		if (bannerImage != null) {
			bannerImage.close();
		}
	}

	/**
	 * set user information
	 */
	public void updateUser(User user) {
		name = user.getUsername();
		description = user.getDescription();
		location = user.getLocation();
		userUrl = user.getProfileUrl();
		profileImageUrl = user.getProfileImageThumbnailUrl();
		bannerImageUrl = user.getBannerImageThumbnailUrl();
	}

	/**
	 * set user information using credentials
	 */
	public void updateCredentials(Credentials credentials) {
		statusPref = new StatusPreferenceUpdate();
		privacy = credentials.isLocked();
		statusPref.setSensitive(credentials.isSensitive());
		statusPref.setLanguage(credentials.getLanguage());
		statusPref.setVisibility(credentials.getVisibility());
	}

	/**
	 * @return screen name of the user
	 */
	public String getUsername() {
		return name;
	}

	/**
	 * @param name profile name
	 */
	public void setUsername(String name) {
		this.name = name;
	}

	/**
	 * @return profile description (bio)
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description profile description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return user location name
	 */
	public String getLocation() {
		return location;
	}

	/**
	 *
	 * @param location user location name
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * get user url attached to the profile
	 *
	 * @return user url
	 */
	public String getUrl() {
		return userUrl;
	}

	/**
	 * set user url
	 */
	public void setUrl(String userUrl) {
		this.userUrl = userUrl;
	}

	/**
	 * @return profile image media instance or null if not added
	 */
	@Nullable
	public MediaStatus getProfileImageMedia() {
		return profileImage;
	}

	/**
	 *
	 */
	public void setProfileImage(MediaStatus profileImage) {
		this.profileImage = profileImage;
	}


	/**
	 * @return banner image media instance or null if not added
	 */
	@Nullable
	public MediaStatus getBannerImageMedia() {
		return bannerImage;
	}

	/**
	 *
	 */
	public void setBannerImage(MediaStatus bannerImage) {
		this.bannerImage = bannerImage;
	}

	/**
	 *
	 */
	@Nullable
	public StatusPreferenceUpdate getStatusPreference() {
		return statusPref;
	}

	/**
	 *
	 */
	public void setStatusPreference(StatusPreferenceUpdate statusPref) {
		this.statusPref = statusPref;
	}

	/**
	 * get profile privacy preference
	 *
	 * @return true to ask user to confirm new followers
	 */
	public boolean isPrivate() {
		return privacy;
	}

	/**
	 * enable/disable follow confirmation
	 */
	public void setPrivacy(boolean privacy) {
		this.privacy = privacy;
	}

	/**
	 * @return profile image thumbnail url
	 */
	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	/**
	 * @return get banner image preview url
	 */
	public String getBannerImageUrl() {
		return bannerImageUrl;
	}

	/**
	 * initialize input streams of the image files
	 * streams must be closed calling {@link #close()}
	 *
	 * @return true if initialization finished without any error
	 */
	public boolean prepare(ContentResolver resolver) {
		return (profileImage == null || profileImage.openStream(resolver)) && (bannerImage == null || bannerImage.openStream(resolver));
	}


	@NonNull
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("name=\"");
		buf.append(getUsername()).append('\"');
		if (!getDescription().isEmpty())
			buf.append(" bio=\"").append(getDescription()).append('\"');
		if (!getLocation().isEmpty())
			buf.append(" location=\"").append(getLocation()).append('\"');
		if (!getUrl().isEmpty())
			buf.append(" url=\"").append(getUrl()).append('\"');
		return buf.toString();
	}
}