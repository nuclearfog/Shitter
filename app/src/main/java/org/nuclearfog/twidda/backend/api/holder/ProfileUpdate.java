package org.nuclearfog.twidda.backend.api.holder;

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

    private Uri imageUrl, bannerUrl;
    private InputStream profileImgStream, bannerImgStream;

    private String name = "";
    private String url = "";
    private String description = "";
    private String location = "";

    /**
     * add profile image Uri
     *
     * @param context       context used to resolve Uri
     * @param imageUrl Uri of the local image file
     */
    public boolean addImageUri(Context context, @NonNull Uri imageUrl) {
        DocumentFile file = DocumentFile.fromSingleUri(context, imageUrl);
        if (file != null && file.exists() && file.canRead() && file.length() > 0) {
            this.imageUrl = imageUrl;
            return true;
        }
        return false;
    }

    public void setProfileInformation(String name, String url, String description, String location) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.location = location;
    }

    /**
     * add banner image Uri
     *
     * @param context      context used to resolve Uri
     * @param bannerUrl  Uri of the local image file
     */
    public boolean addBannerUri(Context context, @NonNull Uri bannerUrl) {
        DocumentFile file = DocumentFile.fromSingleUri(context, bannerUrl);
        if (file != null && file.exists() && file.canRead() && file.length() > 0) {
            this.bannerUrl = bannerUrl;
            return true;
        }
        return false;
    }

    /**
     * initialize inputstreams of the image files
     *
     * @return true if initialization succeded
     */
    public boolean initMedia(ContentResolver resolver) {
        try {
            // open input streams
            if (imageUrl != null) {
                InputStream profileImgStream = resolver.openInputStream(imageUrl);
                if (profileImgStream != null && profileImgStream.available() > 0) {
                    this.profileImgStream = profileImgStream;
                } else {
                    return false;
                }
            }
            if (bannerUrl != null) {
                InputStream bannerImgStream = resolver.openInputStream(bannerUrl);
                if (bannerImgStream != null && bannerImgStream.available() > 0) {
                    this.bannerImgStream = bannerImgStream;
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
        return imageUrl != null || bannerUrl != null;
    }

    /**
     * @return filestream of the profile image
     */
    @Nullable
    public InputStream getProfileImageStream() {
        return profileImgStream;
    }

    /**
     * @return filestream of the banner image
     */
    @Nullable
    public InputStream getBannerImageStream() {
        return bannerImgStream;
    }

    /**
     * close all image streams
     */
    public void closeStreams() {
        try {
            if (profileImgStream != null)
                profileImgStream.close();
            if (bannerImgStream != null)
                bannerImgStream.close();
        } catch (IOException e) {
            // ignore
        }
    }
}