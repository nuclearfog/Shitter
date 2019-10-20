package org.nuclearfog.twidda.backend.items;

public class UserHolder {

    private final String name, link, location, bio, imageLink;

    public UserHolder(String name, String link, String location, String bio, String imageLink) {
        this.name = name;
        this.link = link;
        this.location = location;
        this.imageLink = imageLink;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public String getImageLink() {
        return imageLink;
    }
}