package org.nuclearfog.twidda.backend.api;

import org.json.JSONObject;
import org.nuclearfog.twidda.model.Trend;

/**
 * API v 1.1 implementation for a twitter trend
 *
 * @author nuclearfog
 */
class TrendV1 implements Trend {

    private int rank;
    private int popularity;
    private String name;

    TrendV1(JSONObject json, int rank) {
        name = json.optString("name");
        popularity = json.optInt("tweet_volume");
        this.rank = rank;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public int getPopularity() {
        return popularity;
    }
}