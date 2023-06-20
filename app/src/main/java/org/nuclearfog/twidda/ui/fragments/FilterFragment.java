package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusFilterLoader;
import org.nuclearfog.twidda.backend.async.StatusFilterLoader.StatusFilterResult;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.ui.adapter.FilterAdapter;
import org.nuclearfog.twidda.ui.adapter.FilterAdapter.OnFilterClickListener;

/**
 * status filterlist fragment
 *
 * @author nuclearfog
 */
public class FilterFragment extends ListFragment implements OnFilterClickListener, AsyncCallback<StatusFilterResult> {

	private FilterAdapter adapter;
	private StatusFilterLoader filterLoader;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		adapter = new FilterAdapter(this);
		filterLoader = new StatusFilterLoader(requireContext());
		setAdapter(adapter);

		filterLoader.execute(null, this);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		filterLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		filterLoader.execute(null, this);
	}


	@Override
	protected void onReset() {
		filterLoader.execute(null, this);
		setRefresh(true);
	}


	@Override
	public void onFilterClick(int position) {
		// todo implement this
	}


	@Override
	public void onFilterRemove(int position) {
		// todo implement this
	}


	@Override
	public void onResult(@NonNull StatusFilterResult result) {
		if (result.filters != null) {
			adapter.replaceItems(result.filters);
		} else if (result.exception != null && getContext() != null) {
			ErrorUtils.showErrorMessage(requireContext(), result.exception);
		}
		setRefresh(false);
	}
}