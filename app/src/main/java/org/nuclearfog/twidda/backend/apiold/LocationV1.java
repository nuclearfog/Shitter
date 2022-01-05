package org.nuclearfog.twidda.backend.apiold;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Location;


/**
 * implementation for Twitter4J location
 *
 * @author nuclearfog
 */
class LocationV1 implements Location {

    private String placeName = "";
    private int worldId;


    LocationV1(twitter4j.Location location) {
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

    @Override
    public String getName() {
        return placeName;
    }

    @Override
    public int getId() {
        return worldId;
    }

    @Override
    @NonNull
    public String toString() {
        return placeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocationV1)
            return ((LocationV1) obj).worldId == worldId;
        return false;
    }
}