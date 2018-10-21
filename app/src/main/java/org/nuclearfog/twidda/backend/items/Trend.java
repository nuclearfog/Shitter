package org.nuclearfog.twidda.backend.items;

public class Trend {
    private final String trend;
    private final int position;

    public Trend(int position, String trend) {
        this.position = position;
        this.trend = trend;
    }


    /**
     * get Trend name
     *
     * @return trend name
     */
    public String getName() {
        return trend;
    }


    /**
     * get trend rank
     *
     * @return trend rank
     */
    public int getPosition() {
        return position;
    }
}