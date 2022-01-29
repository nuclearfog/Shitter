package org.nuclearfog.twidda.backend.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Location;

/**
 * API v1.1 implementation of Trend location
 *
 * @author nuclearfog
 */
class LocationV1 implements Location {

    private int id;
    private String name;

    LocationV1(JSONObject json) {
        id = json.optInt("woeid");
        String placeName = json.optString("name");
        String country = json.optString("country");

        if (!country.isEmpty() && !country.equals(placeName))
            name = country + ", " + placeName;
        else
            name = placeName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Location))
            return false;
        return ((Location) obj).getId() == id;
    }

    @NonNull
    @Override
    public String toString() {
        return "id:" + id + " name:\"" + name + "\"";
    }
}