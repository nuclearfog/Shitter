package org.nuclearfog.twidda.backend.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.Configuration;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.ui.activities.StatusActivity;

import java.util.List;
import java.util.regex.Pattern;

/**
 * This class provides methods to open different links, depending on app settings
 *
 * @author nuclearfog
 */
public class LinkUtils {

	/**
	 * regex pattern of a status URL
	 */
	private static final Pattern TWITTER_LINK_PATTERN = Pattern.compile("https://twitter.com/\\w+/status/\\d+");

	private LinkUtils() {
	}

	/**
	 * Open a link, regarding it's content
	 *
	 * @param activity activity used to open link
	 * @param url      url to open
	 */
	public static void openLink(Activity activity, String url) {
		if (!url.startsWith("https://"))
			url = "https://" + url;
		Uri link = Uri.parse(url);
		GlobalSettings settings = GlobalSettings.getInstance(activity);
		// check if the link points to another status
		if ((settings.getLogin().getConfiguration() == Configuration.TWITTER1 || settings.getLogin().getConfiguration() == Configuration.TWITTER2)
				&& TWITTER_LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(activity, StatusActivity.class);
			intent.putExtra(StatusActivity.KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(StatusActivity.KEY_STATUS_NAME, segments.get(0));
			activity.startActivity(intent);
			return;
		} else if (url.startsWith("https://twitter.com") && settings.twitterAltSet()) {
			url = "https://nitter.net" + link.getPath();
			link = Uri.parse(url);
		}
		// open link in a browser
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(link);
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(activity.getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * open location coordinates
	 *
	 * @param activity    activity used to open link
	 * @param coordinates coordinate string
	 */
	public static void openCoordinates(Activity activity, String coordinates) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("geo:" + coordinates + "?z=14"));
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(activity.getApplicationContext(), R.string.error_no_card_app, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * open a link to a media file
	 *
	 * @param activity activity used to open link
	 * @param url      media url
	 */
	public static void openMediaLink(Activity activity, Uri url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(url);
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(activity.getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
		}
	}
}