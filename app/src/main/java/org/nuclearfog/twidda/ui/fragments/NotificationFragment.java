package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.FollowRequestAction;
import org.nuclearfog.twidda.backend.async.NotificationAction;
import org.nuclearfog.twidda.backend.async.NotificationLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.NotificationAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.NotificationAdapter.OnNotificationClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.io.Serializable;

/**
 * fragment to show notifications
 *
 * @author nuclearfog
 */
public class NotificationFragment extends ListFragment implements OnNotificationClickListener, OnConfirmListener, ActivityResultCallback<ActivityResult> {

	/**
	 * Bundle key used to save adapter items
	 * value type is {@link Notification[]}
	 */
	private static final String KEY_DATA = "notification-data";


	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
	private AsyncCallback<NotificationAction.Result> notificationActionCallback = this::onDismiss;
	private AsyncCallback<NotificationLoader.Result> notificationLoaderCallback = this::onNotificationResult;
	private AsyncCallback<FollowRequestAction.Result> followRequestCallback = this::onFollowRequestResult;

	private NotificationLoader notificationLoader;
	private NotificationAction notificationAction;
	private FollowRequestAction followAction;

	private NotificationAdapter adapter;
	private ConfirmDialog confirmDialog;

	@Nullable
	private Notification select;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		notificationLoader = new NotificationLoader(requireContext());
		notificationAction = new NotificationAction(requireContext());
		followAction = new FollowRequestAction(requireContext());
		adapter = new NotificationAdapter(this);
		setAdapter(adapter);

		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof Notifications) {
				adapter.replaceItems((Notifications) data);
				return;
			}
		}
		load(0L, 0L, 0);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		notificationLoader.cancel();
		notificationAction.cancel();
		followAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(adapter.getTopItemId(), 0L, 0);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		notificationLoader = new NotificationLoader(requireContext());
		notificationAction = new NotificationAction(requireContext());
		followAction = new FollowRequestAction(requireContext());
		load(0L, 0L, 0);
		setRefresh(true);
	}


	@Override
	public void onNotificationClick(Notification notification, int action) {
		if (!isRefreshing()) {
			switch (action) {
				case OnNotificationClickListener.NOTIFICATION_VIEW:
					Intent intent = new Intent(requireContext(), StatusActivity.class);
					intent.putExtra(StatusActivity.KEY_DATA, notification);
					activityResultLauncher.launch(intent);
					break;

				case OnNotificationClickListener.NOTIFICATION_DISMISS:
					if (!confirmDialog.isShowing() && notificationAction.isIdle()) {
						confirmDialog.show(ConfirmDialog.NOTIFICATION_DISMISS);
						select = notification;
					}
					break;

				case OnNotificationClickListener.NOTIFICATION_USER:
					if (notification.getType() == Notification.TYPE_REQUEST) {
						if (!confirmDialog.isShowing()) {
							confirmDialog.show(ConfirmDialog.FOLLOW_REQUEST);
							select = notification;
						}
					} else {
						intent = new Intent(requireContext(), ProfileActivity.class);
						intent.putExtra(ProfileActivity.KEY_USER, notification.getUser());
						startActivity(intent);
					}
					break;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(long sinceId, long maxId, int position) {
		if (!isRefreshing() && notificationLoader.isIdle()) {
			load(sinceId, maxId, position);
			return true;
		}
		return false;
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			switch (result.getResultCode()) {
				case StatusActivity.RETURN_NOTIFICATION_UPDATE:
					Object data = intent.getSerializableExtra(StatusActivity.KEY_DATA);
					if (data instanceof Notification) {
						Notification update = (Notification) data;
						adapter.updateItem(update);
					}
					break;

				case StatusActivity.RETURN_NOTIFICATION_REMOVED:
					long notificationId = intent.getLongExtra(StatusActivity.KEY_NOTIFICATION_ID, 0L);
					adapter.removeItem(notificationId);
					break;
			}
		}
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		if (type == ConfirmDialog.NOTIFICATION_DISMISS) {
			if (select != null) {
				NotificationAction.Param param = new NotificationAction.Param(NotificationAction.Param.DISMISS, select.getId());
				notificationAction.execute(param, notificationActionCallback);
			}
		} else if (type == ConfirmDialog.FOLLOW_REQUEST) {
			if (select != null && select.getUser() != null) {
				FollowRequestAction.Param param = new FollowRequestAction.Param(FollowRequestAction.Param.ACCEPT, select.getUser().getId(), select.getId());
				followAction.execute(param, followRequestCallback);
			}
		}
	}

	/**
	 * used by {@link NotificationLoader} to set notification items
	 */
	private void onNotificationResult(@NonNull NotificationLoader.Result result) {
		if (result.notifications != null) {
			adapter.addItems(result.notifications, result.position);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
			adapter.disableLoading();
		}
		setRefresh(false);
	}

	/**
	 * used by {@link FollowRequestAction} to accept a follow request
	 */
	private void onFollowRequestResult(FollowRequestAction.Result result) {
		Context context = getContext();
		if (context != null) {
			if (result.mode == FollowRequestAction.Result.ACCEPT) {
				Toast.makeText(context, R.string.info_follow_request_accepted, Toast.LENGTH_SHORT).show();
				adapter.removeItem(result.notification_id);
			} else if (result.mode == FollowRequestAction.Result.ERROR) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 * called by {@link NotificationAction} to take action on a selected notification
	 */
	private void onDismiss(@NonNull NotificationAction.Result result) {
		if (result.mode == NotificationAction.Result.DISMISS) {
			adapter.removeItem(result.id);
		} else if (result.mode == NotificationAction.Result.ERROR) {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
			if (result.exception != null && result.exception.getErrorCode() == ConnectionException.RESOURCE_NOT_FOUND) {
				adapter.removeItem(result.id);
			}
		}
	}

	/**
	 * @param minId lowest notification ID to load
	 * @param maxId highest notification Id to load
	 * @param pos   index to insert the new items
	 */
	private void load(long minId, long maxId, int pos) {
		NotificationLoader.Param param = new NotificationLoader.Param(NotificationLoader.Param.LOAD_ALL, pos, minId, maxId);
		notificationLoader.execute(param, notificationLoaderCallback);
	}
}