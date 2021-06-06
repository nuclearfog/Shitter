package org.nuclearfog.twidda.backend.model;

import androidx.annotation.NonNull;

import twitter4j.Location;

/**
 * Trend location holder
 *
 * @author nuclearfog
 */
public class TrendLocation {

    private String placeName = "";
    private int worldId;

    /**
     * construct location object from online
     *
     * @param location twitter4j location
     */
    public TrendLocation(Location location) {
        String country = location.getCountryName();
        String placeName = location.getName();

        if (country != null && placeName != null) {
            if (!country.isEmpty() && !country.equals(placeName))
                this.placeName = country + ", " + placeName;
            else
                this.placeName = "" + placeName;
        }
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
        return placeName;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrendLocation)
            return ((TrendLocation) obj).worldId == worldId;
        return false;
    }
}