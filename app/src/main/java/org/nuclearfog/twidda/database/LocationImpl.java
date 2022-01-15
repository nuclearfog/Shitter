package org.nuclearfog.twidda.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.model.Location;

/**
 * database implementation of a location
 *
 * @author nuclearfog
 */
class LocationImpl implements Location {


    private int id;
    private String name;

    /**
     * construct location object from local
     *
     * @param placeName name of locale
     * @param worldId   woe id
     */
    LocationImpl(String placeName, int worldId) {
        this.name = placeName;
        this.id = worldId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
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
        return id + ":" + name;
    }
}