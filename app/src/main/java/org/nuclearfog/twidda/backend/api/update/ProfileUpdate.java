package org.nuclearfog.twidda.backend.api.update;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to upload profile information
 *
 * @author nuclearfog
 */
public class ProfileUpdate {

	private Uri[] imageUrls = new Uri[2];
	private InputStream[] imageStreams = new InputStream[2];

	private String name = "";
	private String url = "";
	private String description = "";
	private String location = "";

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
	 * add profile image Uri
	 *
	 * @param context  context used to resolve Uri
	 * @param imageUrl Uri of the local image file
	 * @return true if file is valid, false otherwise
	 */
	public boolean setImage(Context context, @NonNull Uri imageUrl) {
		DocumentFile file = DocumentFile.fromSingleUri(context, imageUrl);
		if (file != null && file.length() > 0) {
			imageUrls[0] = imageUrl;
			return true;
		}
		return false;
	}

	/**
	 * add banner image Uri
	 *
	 * @param context   context used to resolve Uri
	 * @param bannerUrl Uri of the local image file
	 * @return true if file is valid, false otherwise
	 */
	public boolean setBanner(Context context, @NonNull Uri bannerUrl) {
		DocumentFile file = DocumentFile.fromSingleUri(context, bannerUrl);
		if (file != null && file.length() > 0) {
			imageUrls[1] = bannerUrl;
			return true;
		}
		return false;
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
		return imageUrls[0] != null || imageUrls[1] != null;
	}

	/**
	 * @return filestream of the profile image
	 */
	@Nullable
	public InputStream getProfileImageStream() {
		return imageStreams[0];
	}

	/**
	 * @return filestream of the banner image
	 */
	@Nullable
	public InputStream getBannerImageStream() {
		return imageStreams[1];
	}

	/**
	 * initialize input streams of the image files
	 * streams must be closed calling {@link #close()}
	 *
	 * @return true if initialization finished without any error
	 */
	@SuppressLint("Recycle")
	public boolean prepare(ContentResolver resolver) {
		try {
			for (int i = 0; i < imageUrls.length; i++) {
				if (imageUrls[i] != null) {
					 InputStream profileImgStream = resolver.openInputStream(imageUrls[i]);
					if (profileImgStream != null && profileImgStream.available() > 0) {
						this.imageStreams[i] = profileImgStream;
					} else {
						return false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * close all image streams
	 */
	public void close() {
		try {
			for (InputStream imageStream : imageStreams) {
				if (imageStream != null) {
					imageStream.close();
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}

	@NonNull
	@Override
	public String toString() {
		String result = "name:\"" + name + "\"";
		if (!description.isEmpty())
			result += " bio:\"" + description + "\"";
		if (!location.isEmpty())
			result += " location:\"" + location + "\"";
		if (!url.isEmpty())
			result += " url:\"" + url + "\"";
		result += " image:" + imageAdded();
		return result;
	}
}