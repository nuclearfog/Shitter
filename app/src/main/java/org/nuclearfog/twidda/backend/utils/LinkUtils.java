package org.nuclearfog.twidda.backend.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
		GlobalSettings settings = GlobalSettings.getInstance(activity);
		if (!url.contains("://")) // check if link contains any scheme like 'http://'
			url = "https://" + url;
		Uri link = Uri.parse(url);

		// if it's a link to a Tweet, open Tweet in an activity
		if ((settings.getLogin().getConfiguration() == Configuration.TWITTER1 || settings.getLogin().getConfiguration() == Configuration.TWITTER2)
				&& TWITTER_LINK_PATTERN.matcher(url).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(activity, StatusActivity.class);
			intent.putExtra(StatusActivity.KEY_STATUS_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(StatusActivity.KEY_NAME, segments.get(0));
			activity.startActivity(intent);
		}
		// open link in a browser
		else {
			// replace Twitter link with Nitter if enabled
			if (settings.twitterAltSet() && (url.startsWith("https://twitter.com") || url.startsWith("https://mobile.twitter.com"))) {
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
	 * @param url media url
	 */
	public static void openMediaLink(Context context, Uri url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(url);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException err) {
			Toast.makeText(context.getApplicationContext(), R.string.error_connection_failed, Toast.LENGTH_SHORT).show();
		}
	}
}