package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.MediaStatus;
import org.nuclearfog.twidda.model.Status;

import java.io.Closeable;

/**
 * This class is used to upload profile information
 *
 * @author nuclearfog
 */
public class UserUpdate implements Closeable {

	private MediaStatus profileImage, bannerImage;

	private String name = "";
	private String description = "";
	private String location = "";

	private int statusVisibility = Status.VISIBLE_DEFAULT;
	private boolean sensitiveContent = false;
	private boolean privacy = false;
	private String langCode = "";


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
	 * setup profile information
	 *
	 * @param name        username to update
	 * @param description description of the profile
	 * @param location    location name
	 */
	public void setProfile(String name, String description, String location) {
		this.name = name;
		this.description = description;
		this.location = location;
	}

	/**
	 *
	 */
	public void setProfileImage(MediaStatus profileImage) {
		this.profileImage = profileImage;
	}

	/**
	 *
	 */
	public void setBannerImage(MediaStatus bannerImage) {
		this.bannerImage = bannerImage;
	}

	/**
	 * set default status visibiltiy
	 *
	 * @param statusVisibility default status visibility {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_DEFAULT,Status#VISIBLE_PRIVATE,Status#VISIBLE_UNLISTED}
	 */
	public void setStatusVisibility(int statusVisibility) {
		this.statusVisibility = statusVisibility;
	}

	/**
	 * set default status sensitive flag
	 *
	 * @param sensitiveContent true to set sensitive flag by default
	 */
	public void setContentSensitive(boolean sensitiveContent) {
		this.sensitiveContent = sensitiveContent;
	}

	/**
	 * set default language for posts
	 *
	 * @param langCode lang code
	 */
	public void setLanguageCode(@NonNull String langCode) {
		this.langCode = langCode;
	}

	/**
	 * enable/disable follow confirmation
	 */
	public void setPrivacy(boolean privacy) {
		this.privacy = privacy;
	}

	/**
	 * @return screen name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return profile description (bio)
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return profile image media instance or null if not added
	 */
	@Nullable
	public MediaStatus getProfileImageMedia() {
		return profileImage;
	}

	/**
	 * @return banner image media instance or null if not added
	 */
	@Nullable
	public MediaStatus getBannerImageMedia() {
		return bannerImage;
	}

	/**
	 * get default status visibility
	 *
	 * @return status visibility constant {@link Status#VISIBLE_PUBLIC,Status#VISIBLE_DEFAULT,Status#VISIBLE_PRIVATE,Status#VISIBLE_UNLISTED}
	 */
	public int getStatusVisibility() {
		return statusVisibility;
	}

	/**
	 * @return true if user's status should be marked as sensitive by default
	 */
	public boolean isSensitive() {
		return sensitiveContent;
	}

	/**
	 * get default language code used for posts
	 *
	 * @return language code
	 */
	public String getLanguageCode() {
		return langCode;
	}

	/**
	 * get profile privacy preference
	 *
	 * @return true to ask user to confirm new followers
	 */
	public boolean privacyEnabled() {
		return privacy;
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
		String result = "name=\"" + name + "\"";
		if (!description.isEmpty())
			result += " bio=\"" + description + "\"";
		if (!location.isEmpty())
			result += " location=\"" + location + "\"";
		return result;
	}
}