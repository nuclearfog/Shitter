package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.ui.activities.ProfileActivity.KEY_PROFILE_USER;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_DATA;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.NotificationAdapter;
import org.nuclearfog.twidda.adapter.NotificationAdapter.OnNotificationClickListener;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.NotificationLoader;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Notification;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.User;
import org.nuclearfog.twidda.ui.activities.ProfileActivity;
import org.nuclearfog.twidda.ui.activities.StatusActivity;

import java.util.List;

/**
 * fragment to show notifications
 *
 * @author nuclearfog
 */
public class NotificationFragment extends ListFragment implements OnNotificationClickListener, ActivityResultCallback<ActivityResult> {

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private NotificationLoader notificationAsync;
	private NotificationAdapter adapter;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new NotificationAdapter(requireContext(), this);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (notificationAsync == null) {
			load(0L, 0L, 0);
			setRefresh(true);
		}
	}


	@Override
	public void onDestroyView() {
		if (notificationAsync != null && notificationAsync.getStatus() == RUNNING) {
			notificationAsync.cancel(true);
		}
		super.onDestroyView();
	}


	@Override
	protected void onReload() {
		long sinceId = 0;
		if (!adapter.isEmpty())
			sinceId = adapter.getItemId(0);
		load(sinceId, 0L, 0);
	}


	@Override
	protected void onReset() {
		adapter = new NotificationAdapter(requireContext(), this);
		setAdapter(adapter);
		load(0L, 0L, 0);
	}


	@Override
	public void onStatusClick(Status status) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), StatusActivity.class);
			intent.putExtra(KEY_STATUS_DATA, status);
			activityResultLauncher.launch(intent);
		}
	}


	@Override
	public void onUserClick(User user) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), ProfileActivity.class);
			intent.putExtra(KEY_PROFILE_USER, user);
			startActivity(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long sinceId, long maxId, int position) {
		if (notificationAsync != null && notificationAsync.getStatus() != AsyncTask.Status.RUNNING) {
			load(sinceId, maxId, position);
			return true;
		}
		return false;
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			if (result.getResultCode() == StatusActivity.RETURN_STATUS_REMOVED) {
				long statusId = intent.getLongExtra(StatusActivity.INTENT_STATUS_REMOVED_ID, 0);
				adapter.removeItem(statusId);
			}
			// todo update status (like in StatusFragment)
		}
	}

	/**
	 * called from {@link NotificationLoader} when notifications were loaded successfully
	 *
	 * @param notifications new items
	 * @param position      index where to insert the new items
	 */
	public void onSuccess(@NonNull List<Notification> notifications, int position) {
		adapter.addItems(notifications, position);
		setRefresh(false);
	}

	/**
	 * called from {@link NotificationLoader} if an error occurs
	 */
	public void onError(ConnectionException exception) {
		ErrorHandler.handleFailure(requireContext(), exception);
		adapter.disableLoading();
		setRefresh(false);
	}

	/**
	 * @param minId lowest notification ID to load
	 * @param maxId highest notification Id to load
	 * @param pos   index to insert the new items
	 */
	private void load(long minId, long maxId, int pos) {
		notificationAsync = new NotificationLoader(this, pos);
		notificationAsync.execute(minId, maxId);
	}
}