package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import twitter4j.Location;

public class TrendLocation {

    private final String placeName;
    private final int id;

    public TrendLocation(Location location) {
        String country = location.getCountryName();
        String placeName = location.getName();

        if (country == null || country.trim().isEmpty() || country.equals(placeName))
            this.placeName = placeName;
        else
            this.placeName = country + ", " + placeName;
        this.id = location.getWoeid();
    }


    public TrendLocation(String placeName, int id) {
        this.placeName = placeName;
        this.id = id;
    }


    public String getName() {
        return placeName;
    }


    public int getWoeId() {
        return id;
    }


    @Override
    @NonNull
    public String toString() {
        return placeName != null ? placeName : "";
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrendLocation))
            return false;
        return ((TrendLocation) obj).getWoeId() == id;
    }
}