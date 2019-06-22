package org.nuclearfog.twidda.backend.items;

public class Trend {

    private final String position;
    private final String name;


    public Trend(int pos, String name) {
        this.position = pos + 1 + ".";
        this.name = name;
    }

    /**
     * get Trend name
     *
     * @return trend name
     */
    public String getName() {
        return name;
    }

    /**
     * get trend rank
     *
     * @return trend rank
     */
    public String getPosition() {
        return position;
    }

}