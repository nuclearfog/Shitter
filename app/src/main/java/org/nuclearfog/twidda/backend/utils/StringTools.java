package org.nuclearfog.twidda.backend.utils;

import android.content.res.Resources;
import android.util.Base64;

import org.nuclearfog.twidda.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * this class creates time strings
 *
 * @author nuclearfog
 */
public final class StringTools {

	/**
	 * regex pattern used to get user mentions
	 */
	private static final Pattern MENTION = Pattern.compile("[@][\\w_]+");

	/**
	 * oauth 1.0 signature algorithm
	 */
	public static final String SIGNATURE_ALG = "HMAC-SHA256";

	/**
	 * date format used by API 1.1
	 * e.g. "Mon Jan 17 13:00:12 +0000 2022"
	 */
	private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

	/**
	 * date format used by API 2.0
	 * e.g. "2008-08-15T13:51:34.000Z"
	 */
	private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

	private static final TimeZone TIME_ZONE = TimeZone.getDefault();

	/**
	 * fallback date if parsing failed
	 */
	private static final long DEFAULT_TIME = 0x61D99F64;

	public static final int TIME_TWITTER_V1 = 0xE16A;
	public static final int TIME_TWITTER_V2 = 0x3F5C;
	public static final int TIME_MASTODON = 0x5105;

	/**
	 * random generator used to generate random strings
	 */
	private static Random rand = new Random();

	private StringTools() {
	}

	/**
	 * creates a time string from the difference between the current time and the given time
	 *
	 * @param time time value from which to create a difference
	 * @return time string showing the time difference
	 */
	public static String formatCreationTime(Resources resources, long time) {
		long diff = new Date().getTime() - time;
		if (diff > 2419200000L) { // more than 4 weeks
			return SimpleDateFormat.getDateInstance().format(time);
		}
		if (diff > 604800000L) { // more than a week
			int number = (int) (diff / 604800000L);
			return resources.getQuantityString(R.plurals.n_weeks, number, number);
		}
		if (diff > 86400000L) { // more than a day
			int number = (int) (diff / 86400000L);
			return resources.getQuantityString(R.plurals.n_days, number, number);
		}
		if (diff > 3600000L) { // more than a hour
			int number = (int) (diff / 3600000L);
			return resources.getQuantityString(R.plurals.n_hours, number, number);
		}
		if (diff / 60000L > 0L) { // more than a minute
			int number = (int) (diff / 60000L);
			return resources.getQuantityString(R.plurals.n_minutes, number, number);
		}
		if (diff / 1000L > 0L) {
			int number = (int) (diff / 1000L);
			return resources.getQuantityString(R.plurals.n_seconds, number, number);
		}
		return resources.getString(R.string.time_now);
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
	 * un-escape html based text
	 *
	 * @param text text containing html escapes
	 * @return unescaped text
	 */
	public static String unescapeString(String text) {
		StringBuilder result = new StringBuilder(text);
		for (int i = result.length() - 1; i >= 0; i--) {
			if (result.charAt(i) == '&') {
				if (result.substring(i).startsWith("&amp;"))
					result.replace(i, i + 5, "&");
				else if (result.substring(i).startsWith("&lt;"))
					result.replace(i, i + 4, "<");
				else if (result.substring(i).startsWith("&gt;"))
					result.replace(i, i + 4, ">");
			}
		}
		return result.toString();
	}

	/**
	 * append user mentions in a text to a string
	 *
	 * @param text   text with user mentions
	 * @param author additional text author name
	 * @return mentioned usernames in one string
	 */
	public static String getUserMentions(String text, String author) {
		StringBuilder buf = new StringBuilder();
		Set<String> sorted = new TreeSet<>(String::compareToIgnoreCase);
		Matcher matcher = MENTION.matcher(text);

		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			sorted.add(text.substring(start, end));
		}
		if (!author.isEmpty()) {
			buf.append(author).append(' ');
			sorted.remove(author);
		}
		for (String item : sorted) {
			buf.append(item).append(' ');
		}
		return buf.toString();
	}

	/**
	 * count @username mentions in a string
	 *
	 * @param text text
	 * @return username count
	 */
	public static int countMentions(String text) {
		int result = 0;
		Matcher m = MENTION.matcher(text);
		while (m.find()) {
			result++;
		}
		return result;
	}

	/**
	 * convert time strings from different APIs to the local format
	 *
	 * @param timeStr    Twitter time string
	 * @param timeFormat API format to use {@link #TIME_TWITTER_V1,#TIME_TWITTER_V2,#TIME_MASTODON}
	 * @return date time
	 */
	public static long getTime(String timeStr, int timeFormat) {
		try {
			switch (timeFormat) {
				case TIME_TWITTER_V1:
					Date result = dateFormat1.parse(timeStr);
					if (result != null)
						return result.getTime();
					break;

				case TIME_TWITTER_V2:
					result = dateFormat2.parse(timeStr);
					if (result != null)
						return result.getTime();
					break;

				case TIME_MASTODON:
					result = dateFormat2.parse(timeStr);
					if (result != null) // temporary fix: Mastodon time depends on timezone
						return result.getTime() + TIME_ZONE.getOffset(new Date().getTime());
					break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return DEFAULT_TIME;
	}

	/**
	 * calculate index offset caused by emojies
	 *
	 * @param text  twitter test
	 * @param limit maximum char index
	 * @return offset value
	 */
	public static int calculateIndexOffset(String text, int limit) {
		int offset = 0;
		for (int c = 0; c < limit - 1 && c < text.length() - 1; c++) {
			// determine if a pair of chars represent an emoji
			if (Character.isSurrogatePair(text.charAt(c), text.charAt(c + 1))) {
				offset++;
				limit++;
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
		byte[] randomBytes = new byte[16];
		rand.nextBytes(randomBytes);
		return new String(Base64.encode(randomBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
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
			switch (focus) {
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
	 * @param method    method e.g. POST,GET or PUT
	 * @param endpoint  endpoint URL
	 * @param param     parameter
	 * @param keyString key used to sign
	 * @return key signature
	 */
	public static String sign(String method, String endpoint, String param, String keyString) throws IOException {
		String input = method + "&" + encode(endpoint) + "&" + encode(param);
		return encode(computeSignature(input, keyString));
	}

	/**
	 * calculate sign string
	 *
	 * @param baseString string to sign
	 * @param keyString  key used for sign
	 * @return sign string
	 */
	public static String computeSignature(String baseString, String keyString) throws IOException {
		try {
			SecretKey secretKey = new SecretKeySpec(keyString.getBytes(), SIGNATURE_ALG);
			Mac mac = Mac.getInstance(SIGNATURE_ALG);
			mac.init(secretKey);
			return new String(Base64.encode(mac.doFinal(baseString.getBytes()), Base64.DEFAULT)).trim();
		} catch (Exception e) {
			throw new IOException("error generating signature!");
		}
	}

	/**
	 * formate user profile image link. (remove suffix but keep the file extension if any)
	 *
	 * @param profileImage user profile image
	 * @return formatted link
	 */
	public static String createProfileImageLink(String profileImage) {
		// set profile image url
		int suffix = profileImage.lastIndexOf('_');
		int extension = profileImage.lastIndexOf('.');
		if (suffix > 0 && extension > 0) {
			if (suffix > extension)
				return profileImage.substring(0, suffix);
			else
				return profileImage.substring(0, suffix) + profileImage.substring(extension);
		}
		return profileImage;
	}
}