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

import org.nuclearfog.twidda.backend.async.StatusLoader;
import org.nuclearfog.twidda.backend.async.StatusLoader.StatusParameter;
import org.nuclearfog.twidda.backend.async.StatusLoader.StatusResult;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusActivity;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter;
import org.nuclearfog.twidda.ui.adapter.StatusAdapter.StatusSelectListener;

/**
 * fragment class to show a status list
 *
 * @author nuclearfog
 */
public class StatusFragment extends ListFragment implements StatusSelectListener, AsyncCallback<StatusResult>, ActivityResultCallback<ActivityResult> {

	/**
	 * Key to define what type of status should be loaded
	 * possible values are {@link #STATUS_FRAGMENT_HOME,#STATUS_FRAGMENT_MENTION,#STATUS_FRAGMENT_USER,#STATUS_FRAGMENT_FAVORIT}
	 * and {@link #STATUS_FRAGMENT_REPLY,#STATUS_FRAGMENT_SEARCH,#STATUS_FRAGMENT_USERLIST,#STATUS_FRAGMENT_PUBLIC,#STATUS_FRAGMENT_BOOKMARK}
	 */
	public static final String KEY_STATUS_FRAGMENT_MODE = "status_mode";

	/**
	 * Key to define a search query
	 * value type is String
	 */
	public static final String KEY_STATUS_FRAGMENT_SEARCH = "status_search";

	/**
	 * Key to define a an (status, user, list) ID
	 * value type is Long
	 */
	public static final String KEY_STATUS_FRAGMENT_ID = "status_id";

	/**
	 * setup list for home timeline
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_HOME = 0xE7028B60;

	/**
	 * setup list for status timeline of a specific user
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_USER = 0x4DBEF6CD;

	/**
	 * setup list for favorite timeline of a specific user
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_FAVORIT = 0x8DE749EC;

	/**
	 * setup list for status replies
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_REPLY = 0xAFB5F1C0;

	/**
	 * setup list for search timeline
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_SEARCH = 0x91A71117;

	/**
	 * setup list for userlist timeline
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_USERLIST = 0x43F518F7;

	/**
	 * setup list for public timeline
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_PUBLIC = 0x6125C6D6;

	/**
	 * setup list for bookmark timeline
	 *
	 * @see #KEY_STATUS_FRAGMENT_MODE
	 */
	public static final int STATUS_FRAGMENT_BOOKMARK = 0x7F493A4C;

	/**
	 * replace all items from list
	 */
	public static final int CLEAR_LIST = -1;

	private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

	private StatusLoader statusAsync;
	private StatusAdapter adapter;

	private String search = "";
	private int mode = 0;
	private long id = 0;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle param = getArguments();
		if (param != null) {
			mode = param.getInt(KEY_STATUS_FRAGMENT_MODE, 0);
			id = param.getLong(KEY_STATUS_FRAGMENT_ID, 0);
			search = param.getString(KEY_STATUS_FRAGMENT_SEARCH, "");
		}
		statusAsync = new StatusLoader(requireContext());
		adapter = new StatusAdapter(requireContext(), this);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			load(0L, 0L, CLEAR_LIST);
			setRefresh(true);
		}
	}


	@Override
	protected void onReset() {
		adapter = new StatusAdapter(requireContext(), this);
		setAdapter(adapter);
		load(0L, 0L, CLEAR_LIST);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		statusAsync.cancel();
		super.onDestroy();
	}


	@Override
	public void onActivityResult(ActivityResult result) {
		Intent intent = result.getData();
		if (intent != null) {
			switch (result.getResultCode()) {
				case StatusActivity.RETURN_STATUS_UPDATE:
					Object data = intent.getSerializableExtra(StatusActivity.INTENT_STATUS_UPDATE_DATA);
					if (data instanceof Status) {
						Status statusUpdate = (Status) data;
						adapter.updateItem(statusUpdate);
					}
					break;

				case StatusActivity.RETURN_STATUS_REMOVED:
					long statusId = intent.getLongExtra(StatusActivity.INTENT_STATUS_REMOVED_ID, 0L);
					adapter.removeItem(statusId);
					break;
			}
		}
	}


	@Override
	protected void onReload() {
		long sinceId = 0;
		if (!adapter.isEmpty())
			sinceId = adapter.getItemId(0);
		load(sinceId, 0L, 0);
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
		if (statusAsync.isIdle()) {
			load(minId, maxId, pos);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(StatusResult result) {
		setRefresh(false);
		if (result.statuses != null) {
			if (result.position == CLEAR_LIST) {
				adapter.replaceItems(result.statuses);
			} else {
				adapter.addItems(result.statuses, result.position);
			}
		} else if (getContext() != null) {
			String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			adapter.disableLoading();
			setRefresh(false);
		}
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
			case STATUS_FRAGMENT_HOME:
				request = new StatusParameter(StatusParameter.HOME, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_USER:
				request = new StatusParameter(StatusParameter.USER, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_FAVORIT:
				request = new StatusParameter(StatusParameter.FAVORIT, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_REPLY:
				if (index == CLEAR_LIST)
					request = new StatusParameter(StatusParameter.REPLIES_LOCAL, id, sinceId, maxId, index, search);
				else
					request = new StatusParameter(StatusParameter.REPLIES, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_SEARCH:
				request = new StatusParameter(StatusParameter.SEARCH, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_USERLIST:
				request = new StatusParameter(StatusParameter.USERLIST, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_PUBLIC:
				request = new StatusParameter(StatusParameter.PUBLIC, id, sinceId, maxId, index, search);
				break;

			case STATUS_FRAGMENT_BOOKMARK:
				request = new StatusLoader.StatusParameter(StatusParameter.BOOKMARKS, id, sinceId, maxId, index, search);
				break;

			default:
				return;
		}
		statusAsync.execute(request, this);
	}
}