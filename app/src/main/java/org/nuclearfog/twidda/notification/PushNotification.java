package org.nuclearfog.twidda.notification;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.ui.activities.MainActivity;

/**
 * This class creates app push notification
 *
 * @author nuclearfog
 */
public class PushNotification {

	public static final String NOTIFICATION_NAME = BuildConfig.APPLICATION_ID + " notification";
	public static final String NOTIFICATION_ID_STR = BuildConfig.APPLICATION_ID + ".notification";

	private static final int NOTIFICATION_ID = 0x25281;

	private NotificationManagerCompat notificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private GlobalSettings settings;
	private Context context;

	/**
	 *
	 */
	public PushNotification(Context context) {
		notificationManager = NotificationManagerCompat.from(context);
		notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_ID_STR);
		settings = GlobalSettings.getInstance(context);
		this.context = context;
		// Open MainActivity and select notification tab, if notification view is clicked
		Intent notificationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
		notificationIntent.putExtra(MainActivity.KEY_SELECT_NOTIFICATION, true);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent resultIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
		notificationBuilder.setContentIntent(resultIntent).setPriority(NotificationCompat.PRIORITY_HIGH).setOnlyAlertOnce(true).setAutoCancel(true);
	}

	/**
	 * create push-notification from notifications
	 *
	 * @param notifications new notifications
	 */
	public void createNotification(Notifications notifications) {
		// todo update existing notification and prevent recreating notification
		if (!notifications.isEmpty()) {
			String title = settings.getLogin().getConfiguration().getName();
			String content;
			int icon;
			if (notifications.size() > 1) {
				content = context.getString(R.string.notification_new);
				icon = R.drawable.bell;
			} else {
				Notification notification = notifications.getFirst();
				switch (notification.getType()) {
					case Notification.TYPE_FAVORITE:
						icon = R.drawable.favorite;
						content = context.getString(R.string.notification_favorite, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_REPOST:
						icon = R.drawable.repost;
						content = context.getString(R.string.notification_repost, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_FOLLOW:
						icon = R.drawable.follower;
						content = context.getString(R.string.notification_follow, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_REQUEST:
						icon = R.drawable.follower_request;
						content = context.getString(R.string.notification_request, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_MENTION:
						icon = R.drawable.mention;
						content = context.getString(R.string.notification_mention, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_STATUS:
						icon = R.drawable.post;
						content = context.getString(R.string.notification_status, notification.getUser().getScreenname());
						break;

					case Notification.TYPE_UPDATE:
						icon = R.drawable.post;
						content = context.getString(R.string.notification_edit);
						break;

					case Notification.TYPE_POLL:
						icon = R.drawable.poll;
						content = context.getString(R.string.notification_poll);
						break;

					default:
						icon = R.drawable.bell;
						content = context.getString(R.string.notification_new);
						break;
				}
			}
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || context.checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
				notificationBuilder.setContentTitle(title).setContentText(content).setSmallIcon(icon);
				notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
			}
		}
	}
}