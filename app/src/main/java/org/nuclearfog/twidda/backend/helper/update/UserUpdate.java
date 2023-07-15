package org.nuclearfog.twidda.backend.helper.update;

import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.helper.MediaStatus;

import java.io.Closeable;

/**
 * This class is used to upload profile information
 *
 * @author nuclearfog
 */
public class UserUpdate implements Closeable {

	private MediaStatus profileImage, bannerImage;

	private String name = "";
	private String url = "";
	private String description = "";
	private String location = "";


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
	 * @param url         profile url
	 * @param description description of the profile
	 * @param location    location name
	 */
	public void setProfile(String name, String url, String description, String location) {
		this.name = name;
		this.url = url;
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
	 * @return location name
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @return profile url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return true if any image is added
	 */
	public boolean imageAdded() {
		return profileImage != null || bannerImage != null;
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
		if (!url.isEmpty())
			result += " url=\"" + url + "\"";
		return result;
	}
}