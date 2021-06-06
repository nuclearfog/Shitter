package org.nuclearfog.twidda.backend.model;

/**
 * Twitter Trend Class
 *
 * @author nuclearfog
 */
public class Trend {

    private int rank;
    private int range;
    private String trendName = "";

    /**
     * Construct trend item from twitter
     *
     * @param trend Twitter4J trend
     * @param rank  trend ranking
     */
    public Trend(twitter4j.Trend trend, int rank) {
        if (trend.getName() != null)
            this.trendName = trend.getName();
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
    public Trend(String trendName, int volume, int rank) {
        if (trendName != null)
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
        if (!trendName.contains("\""))
            return "\"" + trendName + "\"";
        return trendName;
    }
}