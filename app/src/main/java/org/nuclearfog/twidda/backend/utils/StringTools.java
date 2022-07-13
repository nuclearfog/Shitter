package org.nuclearfog.twidda.backend.utils;

import static org.nuclearfog.twidda.backend.api.Twitter.SIGNATURE_ALG;

import android.content.res.Resources;
import android.util.Base64;

import org.nuclearfog.twidda.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
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
	 * date format used by API 1.1
	 * e.g. "Mon Jan 17 13:00:12 +0000 2022"
	 */
	private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

	/**
	 * date format used by API 2.0
	 * e.g. "2008-08-15T13:51:34.000Z"
	 */
	private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

	/**
	 * fallback date if parsing failed
	 */
	private static final long DEFAULT_TIME = 0x61D99F64;

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
		if (diff / 60000L > 0) { // more than a minute
			int number = (int) (diff / 60000L);
			return resources.getQuantityString(R.plurals.n_minutes, number, number);
		}
		if (diff / 1000L > 0) {
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
	 * unescapte html based text
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
	 * @param text   text with user mentions (e.g. tweet)
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
	 * convert Twitter API 1.1 date time to long format
	 *
	 * @param timeStr Twitter time string
	 * @return date time
	 */
	public static long getTime1(String timeStr) {
		try {
			Date date = dateFormat1.parse(timeStr);
			if (date != null)
				return date.getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DEFAULT_TIME;
	}

	/**
	 * convert Twitter API 2 date time to long format
	 *
	 * @param timeStr Twitter time string
	 * @return date time
	 */
	public static long getTime2(String timeStr) {
		try {
			Date date = dateFormat2.parse(timeStr);
			if (date != null)
				return date.getTime();
		} catch (Exception e) {
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
}