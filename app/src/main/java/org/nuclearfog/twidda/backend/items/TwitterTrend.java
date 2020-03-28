package org.nuclearfog.twidda.backend.items;

import twitter4j.Trend;

/**
 * Twitter Trend Class
 */
public class TwitterTrend {

    private final int rank;
    private final String trendName;
    private final int range;

    /**
     * Construct trend item from twitter
     *
     * @param trend Twitter4J trend
     * @param rank  trend ranking
     */
    public TwitterTrend(Trend trend, int rank) {
        this.trendName = "" + trend.getName();
        this.range = trend.getTweetVolume();
        this.rank = rank;
    }

    /**
     * Construct trend item from database
     *
     * @param trendName Name of the Trend
     * @param volume    Trend range
     * @param rank      trend ranking
     */
    public TwitterTrend(String trendName, int volume, int rank) {
        this.trendName = trendName;
        this.range = volume;
        this.rank = rank;
    }

    /**
     * get Trend name
     *
     * @return name
     */
    public String getName() {
        return trendName;
    }

    /**
     * get Trend rank
     *
     * @return rank number
     */
    public int getRank() {
        return rank;
    }

    /**
     * get trend rank string
     *
     * @return string of trend rank
     */
    public String getRankStr() {
        return rank + ".";
    }

    /**
     * get Trend range
     *
     * @return amount of tweets in this trend
     */
    public int getRange() {
        return range;
    }

    /**
     * trend containing range info
     *
     * @return true if trend contains range info
     */
    public boolean hasRangeInfo() {
        return range > 0;
    }

    /**
     * convert trend name into search string format
     *
     * @return string to search
     */
    public String getSearchString() {
        if (trendName.startsWith("#"))
            return trendName;
        else {
            if (!trendName.startsWith("\"") && !trendName.endsWith("\""))
                return "\"" + trendName + "\"";
            return trendName;
        }
    }
}