package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
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
import org.nuclearfog.twidda.model.lists.Filters;
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

	/**
	 * Bundle key used to save adapter items
	 * value type is {@link Filters}
	 */
	private static final String KEY_SAVE = "filter-save";

	private AsyncCallback<StatusFilterLoader.Result> filterLoadCallback = this::onFilterLoaded;
	private AsyncCallback<StatusFilterAction.Result> filterRemoveCallback = this::onFilterRemoved;

	private FilterAdapter adapter;
	private StatusFilterLoader filterLoader;
	private StatusFilterAction filterAction;

	private Filter selection;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		filterLoader = new StatusFilterLoader(requireContext());
		filterAction = new StatusFilterAction(requireContext());
		adapter = new FilterAdapter(this);
		setAdapter(adapter, false);

		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Filters) {
				adapter.setItems((Filters) data);
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
			filterLoader.execute(null, filterLoadCallback);
			setRefresh(true);
		}
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
		if (!isRefreshing()) {
			FilterDialog.show(this, filter);
		}
	}


	@Override
	public void onFilterRemove(Filter filter) {
		if (!isRefreshing() && filterAction.isIdle()) {
			if (ConfirmDialog.show(this, ConfirmDialog.FILTER_REMOVE, null)) {
				selection = filter;
			}
		}
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.FILTER_REMOVE) {
			StatusFilterAction.Param param = new StatusFilterAction.Param(StatusFilterAction.Param.DELETE, selection.getId(), null);
			filterAction.execute(param, filterRemoveCallback);
		}
	}


	@Override
	public void onFilterUpdated(Filter filter) {
		adapter.updateItem(filter);
		Context context = getContext();
		if (context != null) {
			Toast.makeText(context, R.string.info_filter_updated, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 *
	 */
	private void onFilterLoaded(StatusFilterLoader.Result result) {
		if (result.filters != null) {
			adapter.setItems(result.filters);
		} else if (result.exception != null) {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
		setRefresh(false);
	}

	/**
	 *
	 */
	private void onFilterRemoved(StatusFilterAction.Result result) {
		if (result.action == StatusFilterAction.Result.DELETE) {
			adapter.removeItem(result.id);
			Context context = getContext();
			if (context != null) {
				Toast.makeText(context, R.string.info_filter_removed, Toast.LENGTH_SHORT).show();
			}
		} else if (result.action == StatusFilterAction.Result.ERROR) {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}
}