package org.nuclearfog.twidda.backend.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.nuclearfog.twidda.R;

/**
 * This class provides methods to open different links, depending on app settings
 *
 * @author nuclearfog
 */
public class LinkUtils {

	/**
	 *
	 */
	private LinkUtils() {
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

	/**
	 * open url with an external browser
	 *
	 * @param url url to open
	 */
	public static void redirectToBrowser(Activity activity, String url) {
		if (!url.contains("://")) // check if link contains any scheme like 'http://'
			url = "https://" + url;
		Uri link = Uri.parse(url);
		// open link in a browser
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(link);
		try {
			activity.startActivity(intent);
		} catch (Exception exception) {
			Toast.makeText(activity.getApplicationContext(), R.string.error_open_link, Toast.LENGTH_SHORT).show();
		}
	}
}