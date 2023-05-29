package org.nuclearfog.twidda.notification;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.nuclearfog.twidda.BuildConfig;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.lists.Notifications;

/**
 * This class creates app push notification
 *
 * @author nuclearfog
 */
public class PushNotification {

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
	}

	/**
	 * create push-notification from notifications
	 * @param notifications new notifications
	 */
	@SuppressLint("MissingPermission")
	public void createNotification(Notifications notifications) {
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
			notificationBuilder.setContentTitle(title).setContentText(content).setSmallIcon(icon).setAutoCancel(true);
			notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		}
	}
}