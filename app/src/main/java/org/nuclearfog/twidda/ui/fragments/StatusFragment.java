package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.ui.activities.StatusActivity.KEY_STATUS_DATA;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.adapter.StatusAdapter;
import org.nuclearfog.twidda.adapter.StatusAdapter.StatusSelectListener;
import org.nuclearfog.twidda.backend.api.ConnectionException;
import org.nuclearfog.twidda.backend.async.StatusLoader;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.Status;
import org.nuclearfog.twidda.ui.activities.StatusActivity;

import java.util.List;

/**
 * fragment class to show a status list
 *
 * @author nuclearfog
 */
public class StatusFragment extends ListFragment implements StatusSelectListener {

	/**
	 * Key to define what type of status should be loaded
	 * possible values are {@link #STATUS_FRAGMENT_HOME,#STATUS_FRAGMENT_MENTION,#STATUS_FRAGMENT_USER}
	 * and {@link #STATUS_FRAGMENT_FAVORIT,#STATUS_FRAGMENT_REPLY,#STATUS_FRAGMENT_SEARCH,#STATUS_FRAGMENT_USERLIST}
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
	 * replace all items from list
	 */
	public static final int CLEAR_LIST = -1;

	/**
	 * request code to check for status changes
	 */
	private static final int REQUEST_STATUS_CHANGED = 0xB90D;

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
		adapter = new StatusAdapter(requireContext(), this);
		setAdapter(adapter);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (statusAsync == null) {
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
		if (statusAsync != null && statusAsync.getStatus() == RUNNING) {
			statusAsync.cancel(true);
		}
		super.onDestroy();
	}


	@Override
	public void onActivityResult(int reqCode, int returnCode, @Nullable Intent intent) {
		super.onActivityResult(reqCode, returnCode, intent);
		if (intent != null && reqCode == REQUEST_STATUS_CHANGED) {
			if (returnCode == StatusActivity.RETURN_STATUS_UPDATE) {
				Object data = intent.getSerializableExtra(StatusActivity.INTENT_STATUS_UPDATE_DATA);
				if (data instanceof Status) {
					Status statusUpdate = (Status) data;
					adapter.updateItem(statusUpdate);
				}
			} else if (returnCode == StatusActivity.RETURN_STATUS_REMOVED) {
				long statusId = intent.getLongExtra(StatusActivity.INTENT_STATUS_REMOVED_ID, 0);
				adapter.removeItem(statusId);
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
			startActivityForResult(intent, REQUEST_STATUS_CHANGED);
		}
	}


	@Override
	public boolean onPlaceholderClick(long minId, long maxId, int pos) {
		if (statusAsync != null && statusAsync.getStatus() != RUNNING) {
			load(minId, maxId, pos);
			return true;
		}
		return false;
	}

	/**
	 * Set status data to list
	 *
	 * @param statuses List of statuses
	 * @param pos      position where statuses should be added
	 */
	public void setData(List<Status> statuses, int pos) {
		if (pos == CLEAR_LIST) {
			adapter.replaceItems(statuses);
		} else {
			adapter.addItems(statuses, pos);
		}
		setRefresh(false);
	}

	/**
	 * called from {@link StatusLoader} if an error occurs
	 */
	public void onError(@Nullable ConnectionException error) {
		ErrorHandler.handleFailure(requireContext(), error);
		adapter.disableLoading();
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
		switch (mode) {
			case STATUS_FRAGMENT_HOME:
				statusAsync = new StatusLoader(this, StatusLoader.HOME, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;

			case STATUS_FRAGMENT_USER:
				statusAsync = new StatusLoader(this, StatusLoader.USER, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;

			case STATUS_FRAGMENT_FAVORIT:
				statusAsync = new StatusLoader(this, StatusLoader.FAVORIT, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;

			case STATUS_FRAGMENT_REPLY:
				if (index == CLEAR_LIST)
					statusAsync = new StatusLoader(this, StatusLoader.REPLIES_OFFLINE, id, search, index);
				else
					statusAsync = new StatusLoader(this, StatusLoader.REPLIES, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;

			case STATUS_FRAGMENT_SEARCH:
				statusAsync = new StatusLoader(this, StatusLoader.SEARCH, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;

			case STATUS_FRAGMENT_USERLIST:
				statusAsync = new StatusLoader(this, StatusLoader.USERLIST, id, search, index);
				statusAsync.execute(sinceId, maxId);
				break;
		}
	}
}