package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.StatusFilterAction;
import org.nuclearfog.twidda.backend.async.StatusFilterLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.Filter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FilterAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.FilterAdapter.OnFilterClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.ui.dialogs.FilterDialog;
import org.nuclearfog.twidda.ui.dialogs.FilterDialog.FilterDialogCallback;

/**
 * status filterlist fragment
 *
 * @author nuclearfog
 */
public class FilterFragment extends ListFragment implements OnFilterClickListener, OnConfirmListener, FilterDialogCallback {

	private AsyncCallback<StatusFilterLoader.Result> filterLoadCallback = this::onFilterLoaded;
	private AsyncCallback<StatusFilterAction.Result> filterRemoveCallback = this::onFilterRemoved;

	private FilterAdapter adapter;
	private StatusFilterLoader filterLoader;
	private StatusFilterAction filterAction;

	private ConfirmDialog confirmDialog;
	private FilterDialog filterDialog;

	private Filter selection;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		filterLoader = new StatusFilterLoader(requireContext());
		filterAction = new StatusFilterAction(requireContext());
		confirmDialog = new ConfirmDialog(requireActivity(), this);
		filterDialog = new FilterDialog(requireActivity(), this);
		adapter = new FilterAdapter(this);
		setAdapter(adapter);

		filterLoader.execute(null, filterLoadCallback);
		setRefresh(true);
	}


	@Override
	public void onDestroy() {
		filterLoader.cancel();
		filterAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		filterLoader.execute(null, filterLoadCallback);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		filterLoader = new StatusFilterLoader(requireContext());
		filterAction = new StatusFilterAction(requireContext());
		filterLoader.execute(null, filterLoadCallback);
		setRefresh(true);
	}


	@Override
	public void onFilterClick(Filter filter) {
		filterDialog.show(filter);
	}


	@Override
	public void onFilterRemove(Filter filter) {
		if (!confirmDialog.isShowing()) {
			selection = filter;
			confirmDialog.show(ConfirmDialog.FILTER_REMOVE);
		}
	}


	@Override
	public void onConfirm(int type, boolean remember) {
		if (type == ConfirmDialog.FILTER_REMOVE) {
			StatusFilterAction.Param param = new StatusFilterAction.Param(StatusFilterAction.Param.DELETE, selection.getId(), null);
			filterAction.execute(param, filterRemoveCallback);
		}
	}


	@Override
	public void onFilterUpdated(Filter filter) {
		adapter.updateItem(filter);
		if (getContext() != null) {
			Toast.makeText(requireContext(), R.string.info_filter_updated, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 */
	private void onFilterLoaded(StatusFilterLoader.Result result) {
		if (result.filters != null) {
			adapter.replaceItems(result.filters);
		} else if (result.exception != null && getContext() != null) {
			ErrorUtils.showErrorMessage(requireContext(), result.exception);
		}
		setRefresh(false);
	}

	/**
	 *
	 */
	private void onFilterRemoved(StatusFilterAction.Result result) {
		if (result.mode == StatusFilterAction.Result.DELETE) {
			adapter.removeItem(result.id);
			if (getContext() != null) {
				Toast.makeText(requireContext(), R.string.info_filter_removed, Toast.LENGTH_SHORT).show();
			}
		} else if (result.mode == StatusFilterAction.Result.ERROR) {
			if (getContext() != null) {
				ErrorUtils.showErrorMessage(requireContext(), result.exception);
			}
		}
	}
}