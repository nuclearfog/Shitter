package org.nuclearfog.twidda.database;

import org.nuclearfog.twidda.model.Location;

class LocationDB implements Location {

    private String placeName;
    private int worldId;

    /**
     * construct location object from local
     *
     * @param placeName name of locale
     * @param worldId   woe id
     */
    LocationDB(String placeName, int worldId) {
        this.placeName = placeName;
        this.worldId = worldId;
    }

    @Override
    public String getName() {
        return placeName;
    }

    @Override
    public int getId() {
        return worldId;
    }
}