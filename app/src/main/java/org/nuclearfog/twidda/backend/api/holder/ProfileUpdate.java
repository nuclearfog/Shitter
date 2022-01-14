package org.nuclearfog.twidda.backend.api.holder;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to upload profile information
 *
 * @author nuclearfog
 */
public class ProfileUpdate {

    private String name;
    private String url;
    private String description;
    private String location;

    private InputStream profileImgStream;
    private InputStream bannerImgStream;

    /**
     * @param name new name of the profile
     * @param url new profile url
     * @param description new description (bio)
     * @param location new location name
     */
    public ProfileUpdate(String name, String url, String description, String location) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.location = location;
    }

    /**
     * add profile image Uri
     *
     * @param context context used to resolve Uri
     * @param profileImgUri Uri of the local image file
     */
    public void addImageUri(Context context, @NonNull Uri profileImgUri) {
        try {
            profileImgStream = context.getContentResolver().openInputStream(profileImgUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * add banner image Uri
     *
     * @param context context used to resolve Uri
     * @param bannerImgUri Uri of the local image file
     */
    public void addBannerUri(Context context, @NonNull Uri bannerImgUri) {
        try {
            bannerImgStream = context.getContentResolver().openInputStream(bannerImgUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}