package org.nuclearfog.twidda.backend.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class creates time strings
 *
 * @author nuclearfog
 */
public final class StringTools {

    private StringTools() {
    }

    /**
     * creates a time string from the difference between the current time and the given time
     *
     * @param time time value from which to create a difference
     * @return time string showing the time difference
     */
    public static String formatCreationTime(long time) {
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
        if (seconds > 0) {
            return seconds + " s";
        }
        return "0 s";
    }

    /**
     * format media time to string
     *
     * @param time duration/current position in ms
     * @return time string
     */
    public static String formatMediaTime(int time) {
        String result = "";
        int seconds = (time / 1000) % 60;
        int minutes = (time / 60000) % 60;

        if (minutes < 10)
            result += "0";
        result += minutes + ":";

        if (seconds < 10)
            result += "0";
        result += seconds;

        return result;
    }

    /**
     * count @username mentions in a string
     *
     * @param text text
     * @return username count
     */
    public static int countMentions(String text) {
        int result = 0;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '@') {
                char next = text.charAt(i + 1);
                if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z') || (next >= '0' && next <= '9') || next == '_') {
                    result++;
                }
            }
        }
        return result;
    }
}