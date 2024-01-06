package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.model.lists.Statuses;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.recyclerview.StatusAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.StatusAdapter.StatusSelectListener;

/**
 * fragment class to show a status list
 *
 * @author nuclearfog
 */
public class StatusFragment extends ListFragment implements StatusSelectListener, AsyncCallback<StatusLoader.Result>, ActivityResultCallback<ActivityResult> {

	/**
	 * Key to define what type of status should be loaded
	 * possible values are {@link #MODE_HOME ,#STATUS_FRAGMENT_MENTION,#STATUS_FRAGMENT_USER,#STATUS_FRAGMENT_FAVORIT}
	 * and {@link #MODE_REPLY ,#STATUS_FRAGMENT_SEARCH,#STATUS_FRAGMENT_USERLIST,#STATUS_FRAGMENT_PUBLIC,#STATUS_FRAGMENT_BOOKMARK}
	 */
	public static final String KEY_MODE = "status_mode";

	/**
	 * Key to define a search query
	 * value type is String
	 */
	public static final String KEY_SEARCH = "status_search";

	/**
	 * Key to define a an (status, user, list) ID
	 * value type is Long
	 */
	public static final String KEY_ID = "status_id";

	/**
	 * key to save adapter items
	 * value type is {@link Statuses}
	 */
	private static final String KEY_SAVE = "status_save";

	/**
	 * setup list for home timeline
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_HOME = 0xE7028B60;

	/**
	 * setup timeline to show user posts without replies
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_USER = 0x4DBEF6CD;

	/**
	 * setup timeline to show all user posts
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_USER_ALL = 0xfb825f97;

	/**
	 * setup list for favorite timeline of a specific user
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_FAVORIT = 0x8DE749EC;

	/**
	 * setup list for status replies
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_REPLY = 0xAFB5F1C0;

	/**
	 * setup list for search timeline
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_SEARCH = 0x91A71117;

	/**
	 * setup list for userlist timeline
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_USERLIST = 0x43F518F7;

	/**
	 * setup list for public timeline
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_PUBLIC = 0x6125C6D6;

	/**
	 * setup list for bookmark timeline
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_BOOKMARK = 0x7F493A4C;

	/**
	 * replace all items from list
	 */
	private static final int CLEAR_LIST = -1;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private StatusLoader statusLoader;
	private StatusAdapter adapter;

	private String search = "";
	private int mode = 0;
	private long id = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		statusLoader = new StatusLoader(requireContext());
		adapter = new StatusAdapter(this, settings.chronologicalTimelineEnabled());
		setAdapter(adapter, settings.chronologicalTimelineEnabled());

		Bundle param = getArguments();
		if (param != null) {
			mode = param.getInt(KEY_MODE, 0);
			id = param.getLong(KEY_ID, 0L);
			search = param.getString(KEY_SEARCH, "");
		}
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Statuses) {
				adapter.setItems((Statuses) data);
			}
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			load(StatusLoader.Param.NO_ID, StatusLoader.Param.NO_ID, CLEAR_LIST);
			setRefresh(true);
		}
	}


	@Override
	public void onDestroy() {
		statusLoader.cancel();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			switch (result.getResultCode()) {
				case StatusActivity.RETURN_STATUS_UPDATE:
					Object data = intent.getSerializableExtra(StatusActivity.KEY_DATA);
					if (data instanceof Status) {
						Status statusUpdate = (Status) data;
						adapter.updateItem(statusUpdate);
					}
					break;

				case StatusActivity.RETURN_STATUS_REMOVED:
					long statusId = intent.getLongExtra(StatusActivity.KEY_STATUS_ID, 0L);
					adapter.removeItem(statusId);
					break;
			}
		}
	}


	@Override
	protected void onReset() {
		adapter = new StatusAdapter(this, settings.chronologicalTimelineEnabled());
		setAdapter(adapter, settings.chronologicalTimelineEnabled());
		statusLoader = new StatusLoader(requireContext());
		load(StatusLoader.Param.NO_ID, StatusLoader.Param.NO_ID, CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	protected void onReload() {
		if (isReversed()) {
			load(StatusLoader.Param.NO_ID, adapter.getTopItemId(), adapter.getItemCount() - 1);
		} else {
			load(adapter.getTopItemId(), StatusLoader.Param.NO_ID, 0);
		}
	}


	@Override
	public void onStatusSelected(Status status) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), StatusActivity.class);
			intent.putExtra(StatusActivity.KEY_DATA, status);
			activityResultLauncher.launch(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long minId, long maxId, int pos) {
		if (!isRefreshing() && statusLoader.isIdle()) {
			load(minId, maxId, pos);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(@NonNull StatusLoader.Result result) {
		if (result.statuses != null) {
			if (result.position == StatusLoader.Result.CLEAR) {
				adapter.setItems(result.statuses);
			} else {
				adapter.addItems(result.statuses, result.position);
			}
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
	 * load content into the list
	 *
	 * @param sinceId ID where to start at
	 * @param maxId   ID where to stop
	 * @param index   index where status list should be added
	 */
	private void load(long sinceId, long maxId, int index) {
		StatusLoader.Param request;
		switch (mode) {
			case MODE_HOME:
				request = new StatusLoader.Param(StatusLoader.Param.HOME, id, sinceId, maxId, index, search);
				break;

			case MODE_USER:
				request = new StatusLoader.Param(StatusLoader.Param.USER, id, sinceId, maxId, index, search);
				break;

			case MODE_USER_ALL:
				request = new StatusLoader.Param(StatusLoader.Param.USER_ALL, id, sinceId, maxId, index, search);
				break;

			case MODE_FAVORIT:
				request = new StatusLoader.Param(StatusLoader.Param.FAVORIT, id, sinceId, maxId, index, search);
				break;

			case MODE_REPLY:
				if (index == CLEAR_LIST)
					request = new StatusLoader.Param(StatusLoader.Param.REPLIES_LOCAL, id, sinceId, maxId, CLEAR_LIST, search);
				else
					request = new StatusLoader.Param(StatusLoader.Param.REPLIES, id, sinceId, maxId, CLEAR_LIST, search);
				break;

			case MODE_SEARCH:
				request = new StatusLoader.Param(StatusLoader.Param.SEARCH, id, sinceId, maxId, index, search);
				break;

			case MODE_USERLIST:
				request = new StatusLoader.Param(StatusLoader.Param.USERLIST, id, sinceId, maxId, index, search);
				break;

			case MODE_PUBLIC:
				request = new StatusLoader.Param(StatusLoader.Param.PUBLIC, id, sinceId, maxId, index, search);
				break;

			case MODE_BOOKMARK:
				request = new StatusLoader.Param(StatusLoader.Param.BOOKMARKS, id, sinceId, maxId, index, search);
				break;

			default:
				return;
		}
		statusLoader.execute(request, this);
	}
}