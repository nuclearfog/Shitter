package org.nuclearfog.twidda.backend.utils;

import android.content.res.Resources;
import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * this class creates time strings
 *
 * @author nuclearfog
 */
public class StringUtils {

	/**
	 * global number formatter
	 */
	public static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

	/**
	 * date format
	 * e.g. "2008-08-15T13:51:34.000Z"
	 */
	private static final SimpleDateFormat defaultDateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

	private static final TimeZone TIME_ZONE = TimeZone.getDefault();

	private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

	/**
	 * fallback date if parsing failed
	 */
	private static final long DEFAULT_TIME = 0x61D99F64;

	/**
	 *
	 */
	public static final int TIME_MASTODON = 0x5105;

	/**
	 * random generator used to generate random strings
	 */
	private static Random rand = new Random();

	private StringUtils() {
	}

	/**
	 * creates a time string from the difference between the current time and the given time
	 *
	 * @param time time value from which to create a difference
	 * @return time string showing the time difference
	 */
	public static String formatCreationTime(Resources resources, long time) {
		long diff = System.currentTimeMillis() - time;
		if (diff > 2419200000L) { // more than 4 weeks
			return SimpleDateFormat.getDateInstance().format(time);
		}
		if (diff > 604800000L) { // more than a week
			int number = (int) (diff / 604800000L);
			return resources.getQuantityString(R.plurals.weeks_ago, number, number);
		}
		if (diff > 86400000L) { // more than a day
			int number = (int) (diff / 86400000L);
			return resources.getQuantityString(R.plurals.days_ago, number, number);
		}
		if (diff > 3600000L) { // more than a hour
			int number = (int) (diff / 3600000L);
			return resources.getQuantityString(R.plurals.hours_ago, number, number);
		}
		if (diff / 60000L > 0L) { // more than a minute
			int number = (int) (diff / 60000L);
			return resources.getQuantityString(R.plurals.minutes_ago, number, number);
		}
		if (diff / 1000L > 0L) {
			int number = (int) (diff / 1000L);
			return resources.getQuantityString(R.plurals.seconds_ago, number, number);
		}
		return resources.getString(R.string.time_now);
	}

	/**
	 * creates a time string from the difference between the current time and the given time
	 *
	 * @param time time value from which to create a difference
	 * @return time string showing the time difference
	 */
	public static String formatExpirationTime(Resources resources, long time) {
		long diff = time - System.currentTimeMillis();
		if (diff > 2419200000L) { // more than 4 week
			return resources.getString(R.string.filter_expiration, SimpleDateFormat.getDateInstance().format(time));
		}
		if (diff > 604800000L) { // more than a week
			int number = (int) (diff / 604800000L);
			return resources.getQuantityString(R.plurals.weeks_remain, number, number);
		}
		if (diff > 86400000L) { // more than a day
			int number = (int) (diff / 86400000L);
			return resources.getQuantityString(R.plurals.days_remain, number, number);
		}
		if (diff > 3600000L) { // more than a hour
			int number = (int) (diff / 3600000L);
			return resources.getQuantityString(R.plurals.hours_remain, number, number);
		}
		if (diff / 60000L > 0L) { // more than a minute
			int number = (int) (diff / 60000L);
			return resources.getQuantityString(R.plurals.minutes_remain, number, number);
		}
		if (diff / 1000L > 0L) {
			int number = (int) (diff / 1000L);
			return resources.getQuantityString(R.plurals.seconds_remain, number, number);
		}
		return "";
	}

	/**
	 * extract text from html doc
	 *
	 * @param text html string
	 * @return text string
	 */
	public static String extractText(String text) {
		try {
			// create newlines at every <br> or <p> tag
			Document jsoupDoc = Jsoup.parse(text);
			jsoupDoc.outputSettings(OUTPUT_SETTINGS);
			jsoupDoc.select("br").after("\\n");
			jsoupDoc.select("p").before("\\n");
			String str = jsoupDoc.html().replace("\\n", "\n");
			text = Jsoup.clean(str, "", Safelist.none(), OUTPUT_SETTINGS);
			text = text.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&nbsp;", "\u00A0");
			if (text.startsWith("\n")) {
				text = text.substring(1);
			}
		} catch (Exception exception) {
			// use fallback text string from json
		}
		return text;
	}

	/**
	 * convert time strings from different APIs to the local format
	 *
	 * @param timeStr    time string
	 * @param timeFormat API format to use {@link #TIME_MASTODON}
	 * @return date time
	 */
	public static long getTime(String timeStr, int timeFormat) {
		try {
			switch (timeFormat) {
				default:
				case TIME_MASTODON:
					Date result = defaultDateformat.parse(timeStr);
					if (result != null)
						return result.getTime() + TIME_ZONE.getOffset(System.currentTimeMillis());
					break;
			}
		} catch (ParseException exception) {
			if (BuildConfig.DEBUG)
				exception.printStackTrace();
		}
		return DEFAULT_TIME;
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
}