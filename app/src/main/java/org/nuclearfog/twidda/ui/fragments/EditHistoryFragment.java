package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.EditHistoryLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.lists.StatusEditHistory;
import org.nuclearfog.twidda.ui.adapter.recyclerview.EditHistoryAdapter;

/**
 * Status edit history fragment
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.ui.activities.EditHistoryActivity
 */
public class EditHistoryFragment extends ListFragment implements AsyncCallback<EditHistoryLoader.Result> {

	/**
	 * bundle key for the status ID to show the history
	 * value type is Long
	 */
	public static final String KEY_ID = "status-id";

	/**
	 * bundle key to save adapter items
	 * value type is {@link StatusEditHistory}
	 */
	private static final String KEY_DATA = "history-save";

	private EditHistoryLoader historyLoader;
	private EditHistoryAdapter adapter;

	private long id;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		historyLoader = new EditHistoryLoader(requireContext());
		adapter = new EditHistoryAdapter();
		setAdapter(adapter, false);

		if (getArguments() != null) {
			id = getArguments().getLong(KEY_ID);
		}
		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof StatusEditHistory) {
				adapter.setItems((StatusEditHistory) data);
			}
		}
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			historyLoader.execute(id, this);
			setRefresh(true);
		}
	}


	@Override
	public void onDestroy() {
		historyLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		historyLoader.execute(id, this);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		historyLoader = new EditHistoryLoader(requireContext());
		historyLoader.execute(id, this);
		setRefresh(true);
	}


	@Override
	public void onResult(@NonNull EditHistoryLoader.Result result) {
		if (result.history != null) {
			adapter.setItems(result.history);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
		setRefresh(false);
	}
}
