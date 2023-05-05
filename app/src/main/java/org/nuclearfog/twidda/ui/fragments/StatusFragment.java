package org.nuclearfog.twidda.ui.fragments;

import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_DATA;

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

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusLoader;
import org.nuclearfog.twidda.backend.async.StatusLoader.StatusParameter;
import org.nuclearfog.twidda.backend.async.StatusLoader.StatusResult;
import org.nuclearfog.twidda.lists.Statuses;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter.StatusSelectListener;

import java.io.Serializable;

/**
 * fragment class to show a status list
 *
 * @author nuclearfog
 */
public class StatusFragment extends ListFragment implements StatusSelectListener, AsyncCallback<StatusResult>, ActivityResultCallback<ActivityResult> {

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
	 * setup list for status timeline of a specific user
	 *
	 * @see #KEY_MODE
	 */
	public static final int MODE_USER = 0x4DBEF6CD;

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
		adapter = new StatusAdapter(requireContext(), this);
		setAdapter(adapter);

		Bundle param = getArguments();
		if (param != null) {
			mode = param.getInt(KEY_MODE, 0);
			id = param.getLong(KEY_ID, 0);
			search = param.getString(KEY_SEARCH, "");
		}
		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Statuses) {
				adapter.replaceItems((Statuses) data);
				return;
			}
		}
		load(StatusParameter.NO_ID, StatusParameter.NO_ID, StatusAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
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
					Object data = intent.getSerializableExtra(StatusActivity.RETURN_STATUS_UPDATE_DATA);
					if (data instanceof Status) {
						Status statusUpdate = (Status) data;
						adapter.updateItem(statusUpdate);
					}
					break;

				case StatusActivity.RETURN_STATUS_REMOVED:
					long statusId = intent.getLongExtra(StatusActivity.RETURN_STATUS_REMOVED_ID, 0L);
					adapter.removeItem(statusId);
					break;
			}
		}
	}


	@Override
	protected void onReset() {
		adapter.clear();
		load(StatusParameter.NO_ID, StatusParameter.NO_ID, StatusAdapter.CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	protected void onReload() {
		load(adapter.getTopItemId(), StatusParameter.NO_ID, 0);
	}


	@Override
	public void onStatusSelected(Status status) {
		if (!isRefreshing()) {
			Intent intent = new Intent(requireContext(), StatusActivity.class);
			intent.putExtra(KEY_STATUS_DATA, status);
			activityResultLauncher.launch(intent);
		}
	}


	@Override
	public boolean onPlaceholderClick(long minId, long maxId, int pos) {
		if (statusLoader.isIdle()) {
			load(minId, maxId, pos);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(@NonNull StatusResult result) {
		if (result.statuses != null) {
			if (result.position == StatusResult.CLEAR) {
				adapter.replaceItems(result.statuses);
			} else {
				adapter.addItems(result.statuses, result.position);
			}
		} else if (getContext() != null) {
			String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
		StatusParameter request;
		switch (mode) {
			case MODE_HOME:
				request = new StatusParameter(StatusParameter.HOME, id, sinceId, maxId, index, search);
				break;

			case MODE_USER:
				request = new StatusParameter(StatusParameter.USER, id, sinceId, maxId, index, search);
				break;

			case MODE_FAVORIT:
				request = new StatusParameter(StatusParameter.FAVORIT, id, sinceId, maxId, index, search);
				break;

			case MODE_REPLY:
				if (index == StatusAdapter.CLEAR_LIST)
					request = new StatusParameter(StatusParameter.REPLIES_LOCAL, id, sinceId, maxId, index, search);
				else
					request = new StatusParameter(StatusParameter.REPLIES, id, sinceId, maxId, index, search);
				break;

			case MODE_SEARCH:
				request = new StatusParameter(StatusParameter.SEARCH, id, sinceId, maxId, index, search);
				break;

			case MODE_USERLIST:
				request = new StatusParameter(StatusParameter.USERLIST, id, sinceId, maxId, index, search);
				break;

			case MODE_PUBLIC:
				request = new StatusParameter(StatusParameter.PUBLIC, id, sinceId, maxId, index, search);
				break;

			case MODE_BOOKMARK:
				request = new StatusLoader.StatusParameter(StatusParameter.BOOKMARKS, id, sinceId, maxId, index, search);
				break;

			default:
				return;
		}
		statusLoader.execute(request, this);
	}
}