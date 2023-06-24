package org.nuclearfog.twidda.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.NotificationAction;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionParam;
import org.nuclearfog.twidda.backend.async.NotificationAction.NotificationActionResult;
import org.nuclearfog.twidda.backend.async.NotificationLoader;
import org.nuclearfog.twidda.backend.async.NotificationLoader.NotificationLoaderParam;
import org.nuclearfog.twidda.backend.async.NotificationLoader.NotificationLoaderResult;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.model.lists.Notifications;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.NotificationAdapter;
import org.nuclearfog.twidda.ui.adapter.NotificationAdapter.OnNotificationClickListener;
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
	private AsyncCallback<NotificationActionResult> notificationActionCallback = this::onDismiss;
	private AsyncCallback<NotificationLoaderResult> notificationLoaderCallback = this::onResult;

	private NotificationLoader notificationLoader;
	private NotificationAction notificationAction;
	private NotificationAdapter adapter;
	private ConfirmDialog confirmDialog;

	private Notification select;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		notificationLoader = new NotificationLoader(requireContext());
		notificationAction = new NotificationAction(requireContext());
		adapter = new NotificationAdapter(requireContext(), this);
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
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(adapter.getTopItemId(), 0L, 0);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		load(0L, 0L, 0);
		setRefresh(true);
	}


	@Override
	public void onNotificationClick(Notification notification, int action) {
		if (!isRefreshing()) {
			switch (action) {
				case OnNotificationClickListener.VIEW:
					Intent intent = new Intent(requireContext(), StatusActivity.class);
					intent.putExtra(StatusActivity.KEY_DATA, notification);
					activityResultLauncher.launch(intent);
					break;

				case OnNotificationClickListener.DISMISS:
					confirmDialog.show(ConfirmDialog.NOTIFICATION_DISMISS);
					select = notification;
					break;
			}
		}
	}


	@Override
	public void onUserClick(User user) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), ProfileActivity.class);
			intent.putExtra(ProfileActivity.KEY_USER, user);
			startActivity(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long sinceId, long maxId, int position) {
		if (notificationLoader.isIdle()) {
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
	public void onConfirm(int type) {
		if (type == ConfirmDialog.NOTIFICATION_DISMISS) {
			if (select != null) {
				NotificationActionParam param = new NotificationActionParam(NotificationActionParam.DISMISS, select.getId());
				notificationAction.execute(param, notificationActionCallback);
			}
		}
	}


	private void onResult(@NonNull NotificationLoaderResult result) {
		if (result.notifications != null) {
			adapter.addItems(result.notifications, result.position);
		} else {
			if (getContext() != null) {
				ErrorUtils.showErrorMessage(getContext(), result.exception);
			}
			adapter.disableLoading();
		}
		setRefresh(false);
	}

	/**
	 *
	 */
	private void onDismiss(@NonNull NotificationActionResult result) {
		if (result.mode == NotificationActionResult.DISMISS) {
			adapter.removeItem(result.id);
		} else if (result.mode == NotificationActionResult.ERROR) {
			if (getContext() != null) {
				ErrorUtils.showErrorMessage(getContext(), result.exception);
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
		NotificationLoaderParam param = new NotificationLoaderParam(NotificationLoaderParam.LOAD_ALL, pos, minId, maxId);
		notificationLoader.execute(param, notificationLoaderCallback);
	}
}