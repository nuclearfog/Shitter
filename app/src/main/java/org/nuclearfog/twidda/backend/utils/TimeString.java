package org.nuclearfog.twidda.backend.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class creates time strings
 */
public final class TimeString {

    private TimeString() {
    }

    /**
     * creates a time string from the difference between the current time and the given time
     *
     * @param time time value from which to create a difference
     * @return time string showing the time difference
     */
    public static String getTimeString(long time) {
        long diff = new Date().getTime() - time;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if (weeks > 4) {
            Date tweetDate = new Date(time);
            return SimpleDateFormat.getDateInstance().format(tweetDate);
        }
        if (weeks > 0) {
            return weeks + " w";
        }
        if (days > 0) {
            return days + " d";
        }
        if (hours > 0) {
            return hours + " h";
        }
        if (minutes > 0) {
            return minutes + " m";
        }
        return seconds + " s";
    }
}