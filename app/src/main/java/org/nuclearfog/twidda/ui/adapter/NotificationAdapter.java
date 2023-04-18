package org.nuclearfog.twidda.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.backend.async.EmojiLoader;
import org.nuclearfog.twidda.backend.image.PicassoBuilder;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.adapter.holder.OnHolderClickListener;
import org.nuclearfog.twidda.ui.adapter.holder.PlaceHolder;
import org.nuclearfog.twidda.ui.adapter.holder.StatusHolder;
import org.nuclearfog.twidda.ui.adapter.holder.UserHolder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Rycyclerview adapter for notifications
 *
 * @author nuclearfog
 */
public class NotificationAdapter extends Adapter<ViewHolder> implements OnHolderClickListener {

	/**
	 * Minimum count of new statuses to insert a placeholder.
	 */
	private static final int MIN_COUNT = 2;

	private static final int NO_LOADING = -1;

	/**
	 * notification placeholder
	 */
	private static final int TYPE_PLACEHOLER = 0;

	/**
	 * notifcation type for statuses
	 */
	private static final int TYPE_STATUS = 1;

	/**
	 * notification type for users
	 */
	private static final int TYPE_USER = 2;

	private Picasso picasso;
	private GlobalSettings settings;
	private OnNotificationClickListener listener;
	private EmojiLoader emojiLoader;

	private List<Notification> notifications;
	private int loadingIndex;


	public NotificationAdapter(Context context, OnNotificationClickListener listener) {
		settings = GlobalSettings.getInstance(context);
		picasso = PicassoBuilder.get(context);
		emojiLoader = new EmojiLoader(context);
		notifications = new LinkedList<>();
		loadingIndex = NO_LOADING;
		this.listener = listener;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_STATUS) {
			return new StatusHolder(parent, settings, picasso, emojiLoader, this);
		} else if (viewType == TYPE_USER) {
			return new UserHolder(parent, settings, picasso, emojiLoader, this, false);
		} else {
			return new PlaceHolder(parent, settings, false, this);
		}
	}


	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Notification item = notifications.get(position);
		if (item != null) {
			if (holder instanceof StatusHolder && item.getStatus() != null) {
				StatusHolder statusHolder = (StatusHolder) holder;
				statusHolder.setContent(item.getStatus());
				statusHolder.setLabel(item);
			} else if (holder instanceof UserHolder && item.getUser() != null) {
				UserHolder userHolder = (UserHolder) holder;
				userHolder.setContent(item.getUser());
				userHolder.setLabel(item);
			}
		} else if (holder instanceof PlaceHolder) {
			PlaceHolder placeHolder = (PlaceHolder) holder;
			placeHolder.setLoading(loadingIndex == position);
		}
	}


	@Override
	public int getItemCount() {
		return notifications.size();
	}


	@Override
	public int getItemViewType(int position) {
		Notification item = notifications.get(position);
		if (item == null)
			return TYPE_PLACEHOLER;
		switch (item.getType()) {
			default:
				return TYPE_PLACEHOLER;

			case Notification.TYPE_FAVORITE:
			case Notification.TYPE_MENTION:
			case Notification.TYPE_REPOST:
			case Notification.TYPE_POLL:
			case Notification.TYPE_STATUS:
			case Notification.TYPE_UPDATE:
				return TYPE_STATUS;

			case Notification.TYPE_FOLLOW:
			case Notification.TYPE_REQUEST:
				return TYPE_USER;
		}
	}


	@Override
	public boolean onPlaceholderClick(int index) {
		long sinceId = 0;
		long maxId = 0;
		if (index == 0) {
			if (notifications.size() > 1) {
				Notification notification = notifications.get(index + 1);
				if (notification != null) {
					sinceId = notification.getId();
				}
			}
		} else if (index == notifications.size() - 1) {
			Notification notification = notifications.get(index - 1);
			if (notification != null) {
				maxId = notification.getId() - 1;
			}
		} else {
			Notification notification = notifications.get(index + 1);
			if (notification != null) {
				sinceId = notification.getId();
			}
			notification = notifications.get(index - 1);
			if (notification != null) {
				maxId = notification.getId() - 1;
			}
		}
		boolean success = listener.onPlaceholderClick(sinceId, maxId, index);
		if (success) {
			loadingIndex = index;
			return true;
		}
		return false;
	}


	@Override
	public void onItemClick(int position, int type, int... extras) {
		Notification item = notifications.get(position);
		switch (type) {
			case OnHolderClickListener.USER_CLICK:
			case OnHolderClickListener.STATUS_LABEL:
				if (item != null && item.getUser() != null) {
					listener.onUserClick(item.getUser());
				}
				break;

			case OnHolderClickListener.STATUS_CLICK:
				if (item != null) {
					listener.onNotificationClick(item, OnNotificationClickListener.VIEW);
				}
				break;

			case OnHolderClickListener.NOTIFICATION_DISMISS:
				if (item != null) {
					listener.onNotificationClick(item, OnNotificationClickListener.DISMISS);
				}
				break;
		}
	}

	/**
	 * get adapter items
	 *
	 * @return array of notification items
	 */
	public Notification[] getItems() {
		return notifications.toArray(new Notification[0]);
	}

	/**
	 * add new items at specific position
	 *
	 * @param newItems items to add
	 * @param index    position where to add the items
	 */
	public void addItems(List<Notification> newItems, int index) {
		disableLoading();
		if (newItems.size() > MIN_COUNT) {
			if (notifications.isEmpty() || notifications.get(index) != null) {
				// Add placeholder
				notifications.add(index, null);
				notifyItemInserted(index);
			}
		} else if (!notifications.isEmpty() && notifications.get(index) == null) {
			// remove placeholder
			notifications.remove(index);
			notifyItemRemoved(index);
		}
		if (!newItems.isEmpty()) {
			notifications.addAll(index, newItems);
			notifyItemRangeInserted(index, newItems.size());
		}
	}

	/**
	 * Replace all items in the list
	 *
	 * @param newItems list of statuses to add
	 */
	public void replaceItems(Notification[] newItems) {
		notifications.clear();
		notifications.addAll(Arrays.asList(newItems));
		if (newItems.length > MIN_COUNT) {
			notifications.add(null);
		}
		loadingIndex = NO_LOADING;
		notifyDataSetChanged();
	}

	/**
	 * update single item
	 *
	 * @param update notification to update
	 */
	public void updateItem(Notification update) {
		int index = notifications.indexOf(update);
		if (index >= 0) {
			notifications.set(index, update);
			notifyItemChanged(index);
		}
	}

	/**
	 * remove notification
	 *
	 * @param id notification ID
	 */
	public void removeItem(long id) {
		for (int i = 0; i < notifications.size(); i++) {
			Notification item = notifications.get(i);
			if (item != null && item.getId() == id) {
				notifications.remove(i);
				notifyItemRemoved(i);
				break;
			}
		}
	}

	/**
	 * clear adapter data
	 */
	public void clear() {
		notifications.clear();
		notifyDataSetChanged();
	}

	/**
	 * get ID of the first notification item
	 *
	 * @return notification ID
	 */
	public long getTopItemId() {
		if (!notifications.isEmpty() && notifications.get(0) != null) {
			return notifications.get(0).getId();
		}
		return 0L;
	}

	/**
	 * disable placeholder load animation
	 */
	public void disableLoading() {
		if (loadingIndex != NO_LOADING) {
			int oldIndex = loadingIndex;
			loadingIndex = NO_LOADING;
			notifyItemChanged(oldIndex);
		}
	}

	/**
	 * notification item listener
	 */
	public interface OnNotificationClickListener {

		/**
		 * show a notification
		 */
		int VIEW = 1;

		/**
		 * dismiss a notification
		 */
		int DISMISS = 2;

		/**
		 * called on notification click
		 *
		 * @param notification clicked notification
		 * @param action       action {@link #VIEW,#DISMISS}
		 */
		void onNotificationClick(Notification notification, int action);

		/**
		 * called on user item click
		 *
		 * @param user clicked user
		 */
		void onUserClick(User user);

		/**
		 * called on placeholder click
		 *
		 * @param sinceId  notification ID below the placeholder
		 * @param maxId    notification ID over the placeholder
		 * @param position position of the placeholder
		 * @return true to enable loading animation
		 */
		boolean onPlaceholderClick(long sinceId, long maxId, int position);
	}
}