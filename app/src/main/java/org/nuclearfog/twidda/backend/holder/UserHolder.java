package org.nuclearfog.twidda.backend.holder;

import androidx.annotation.NonNull;

/**
 * User information holder ofr updating user information
 */
public class UserHolder {

    private final String name, link, location, bio, profileImage, profileBanner;

    /**
     * create user information holder
     *
     * @param name          user name
     * @param link          profile link
     * @param location      profile location string
     * @param bio           description string
     * @param profileImage  local profile image path
     * @param profileBanner local profile image path
     */
    public UserHolder(String name, String link, String location, String bio, String profileImage, String profileBanner) {
        this.name = name;
        this.bio = bio;
        this.link = link;
        this.location = location;
        this.profileImage = profileImage;
        this.profileBanner = profileBanner;

    }

    /**
     * get sser name
     *
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * get profile link
     *
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * get location
     *
     * @return location name
     */
    public String getLocation() {
        return location;
    }

    /**
     * get profile description
     *
     * @return profile bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * get local image path
     *
     * @return image path
     */
    public String getProfileImage() {
        return profileImage;
    }

    /**
     * check if profile image path is included
     *
     * @return true if image path is included
     */
    public boolean hasProfileImage() {
        return profileImage != null && !profileImage.isEmpty();
    }

    /**
     * getprofile banner path
     *
     * @return image path
     */
    public String getProfileBanner() {
        return profileBanner;
    }

    /**
     * check if profile banner path is included
     *
     * @return true if path is included
     */
    public boolean hasProfileBanner() {
        return profileBanner != null && !profileBanner.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "name=" + name + ", location=" + location + ", link=" + link + "\n" + bio;
    }
}