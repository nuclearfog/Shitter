package org.nuclearfog.twidda.backend.items;

public class Trend {
    public final String trend;
    public final String link;
    public final int position;

    public Trend(int position, String trend, String link) {
        this.position = position;
        this.trend = trend;
        this.link = link;
    }
}
