package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

/**
 * User information holder ofr updating user information
 */
public class UserHolder {

    private final String name, link, location, bio, imageLink;

    /**
     * create user information holder
     *
     * @param name      user name
     * @param link      profile link
     * @param location  profile location string
     * @param bio       description string
     * @param imageLink local profile image path
     */
    public UserHolder(String name, String link, String location, String bio, String imageLink) {
        this.name = name;
        this.link = link;
        this.location = location;
        this.imageLink = imageLink;
        this.bio = bio;
    }

    /**
     * get sser name
     * @return user name
     */
    public String getName() {
        return name;
    }

    /**
     * get profile link
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * get location
     * @return location name
     */
    public String getLocation() {
        return location;
    }

    /**
     * get profile description
     * @return profile bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * get local image path
     * @return image path
     */
    public String getImageLink() {
        return imageLink;
    }

    /**
     * check if profile image path is included
     *
     * @return true if image path is included
     */
    public boolean hasProfileImage() {
        return !imageLink.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "name=" + name + ", location=" + location + ", link=" + link + "\n" + bio;
    }
}