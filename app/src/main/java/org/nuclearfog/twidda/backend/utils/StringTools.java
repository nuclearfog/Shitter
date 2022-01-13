package org.nuclearfog.twidda.backend.utils;

import static org.nuclearfog.twidda.backend.api.Twitter.SIGNATURE_ALG;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * this class creates time strings
 *
 * @author nuclearfog
 */
public final class StringTools {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
    private static final long DEFAULT_TIME = 0x61D99F64;

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

    /**
     * convert Twitter ISO 8601 date time to long format
     *
     * @param timeStr Twitter time string
     * @return date time
     */
    public static long getTime(String timeStr) {
        try {
            Date date = sdf.parse(timeStr);
            if (date != null)
                return date.getTime();
        } catch (Exception e) {
            // make date invalid so it will be not shown
            e.printStackTrace();
        }
        return DEFAULT_TIME;
    }

    /**
     * extract API name from Twitter href string
     *
     * @param srcHref twitter API href
     * @return API name
     */
    public static String getSource(String srcHref) {
        int start = srcHref.indexOf('>') + 1;
        int end = srcHref.lastIndexOf('<');
        if (start > 0 && end > start)
            return srcHref.substring(start, end);
        return srcHref;
    }

    /**
     * calculate index offset caused by emojies
     *
     * @param text twitter test
     * @param limit maximum char index
     * @return offset value
     */
    public static int calculateIndexOffset(String text, int limit) {
        int offset = 0;
        for (int c = 0; c < limit - 1 && c < text.length(); c++) {
            // determine if a pair of chars represent an emoji
            if (Character.isSurrogatePair(text.charAt(c), text.charAt(c + 1))) {
                offset++;
            }
        }
        return offset;
    }

    /**
     * get current timestamp in seconds
     *
     * @return timestamp string
     */
    public static String getTimestamp() {
        return Long.toString(new Date().getTime() / 1000);
    }

    /**
     * create random string
     *
     * @return random percentaged string
     */
    public static String getRandomString() {
        String rand = UUID.randomUUID().toString();
        return new String(Base64.encode(rand.getBytes(), Base64.NO_PADDING | Base64.NO_WRAP));
    }

    /**
     * convert to percentage string (RFC 3986)
     *
     * @param value string to convert
     * @return percentage string
     */
    public static String encode(String value) {
        try {
            value = URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        StringBuilder buffer = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char focus = value.charAt(i);
            switch(focus) {
                case '*':
                    buffer.append("%2A");
                    break;

                case '+':
                    buffer.append("%20");
                    break;

                case ' ':
                    buffer.append('+');
                    break;

                case '%': // replace %7E with '~'
                    if ((i + 1) < value.length() && value.charAt(i + 1) == '7' && value.charAt(i + 2) == 'E') {
                        buffer.append('~');
                        i += 2;
                        break;
                    }

                default:
                    buffer.append(focus);
                    break;
            }
        }
        return buffer.toString();
    }

    /**
     * generate signature for oauth
     *
     * @param method method e.g. POST,GET or PUT
     * @param endpoint endpoint URL
     * @param param parameter
     * @param keyString key used to sign
     * @return key signature
     */
    public static String sign(String method, String endpoint, String param, String keyString) {
        String input = method + "&" + encode(endpoint) + "&" + encode(param);
        return encode(computeSignature(input, keyString));
    }

    /**
     * calculate sign string
     *
     * @param baseString string to sign
     * @param keyString key used for sign
     * @return sign string
     */
    public static String computeSignature(String baseString, String keyString) {
        try {
            SecretKey secretKey = new SecretKeySpec(keyString.getBytes(), SIGNATURE_ALG);
            Mac mac = Mac.getInstance(SIGNATURE_ALG);
            mac.init(secretKey);
            return new String(Base64.encode(mac.doFinal(baseString.getBytes()), Base64.DEFAULT)).trim();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }
}