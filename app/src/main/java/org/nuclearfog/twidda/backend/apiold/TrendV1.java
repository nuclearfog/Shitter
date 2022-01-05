package org.nuclearfog.twidda.backend.apiold;

import org.nuclearfog.twidda.model.Trend;

/**
 * Twitter Trend implementation for Twitter4J
 *
 * @author nuclearfog
 */
class TrendV1 implements Trend {

    private int rank;
    private int range;
    private String trendName = "";


    TrendV1(twitter4j.Trend trend, int rank) {
        if (trend.getName() != null)
            this.trendName = trend.getName();
        this.range = trend.getTweetVolume();
        this.rank = rank;
    }

    @Override
    public String getName() {
        return trendName;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public int getPopularity() {
        return range;
    }
}