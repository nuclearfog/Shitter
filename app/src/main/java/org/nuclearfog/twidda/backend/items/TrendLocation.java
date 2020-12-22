package org.nuclearfog.twidda.backend.items;

import androidx.annotation.NonNull;

import twitter4j.Location;

/**
 * Trend location holder
 */
public class TrendLocation {

    private final String placeName;
    private final int worldId;

    /**
     * construct location object from online
     *
     * @param location twitter4j location
     */
    public TrendLocation(Location location) {
        String country = location.getCountryName();
        String placeName = location.getName();

        if (country == null || country.trim().isEmpty() || country.equals(placeName))
            this.placeName = "" + placeName;
        else
            this.placeName = country + ", " + placeName;
        this.worldId = location.getWoeid();
    }

    /**
     * construct location object from local
     *
     * @param placeName name of locale
     * @param worldId   woe id
     */
    public TrendLocation(String placeName, int worldId) {
        this.placeName = placeName;
        this.worldId = worldId;
    }

    /**
     * get place name
     *
     * @return country followed by place
     */
    public String getName() {
        return placeName;
    }

    /**
     * get World ID
     *
     * @return woeID
     */
    public int getWoeId() {
        return worldId;
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
        return ((TrendLocation) obj).getWoeId() == worldId;
    }
}