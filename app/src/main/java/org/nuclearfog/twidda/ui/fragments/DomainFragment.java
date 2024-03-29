package org.nuclearfog.twidda.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DomainAction;
import org.nuclearfog.twidda.backend.async.DomainLoader;
import org.nuclearfog.twidda.backend.utils.ErrorUtils;
import org.nuclearfog.twidda.model.lists.Domains;
import org.nuclearfog.twidda.ui.adapter.recyclerview.DomainAdapter;
import org.nuclearfog.twidda.ui.adapter.recyclerview.DomainAdapter.OnDomainClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * Fragment containing a list of domain names
 *
 * @author nuclearfog
 */
public class DomainFragment extends ListFragment implements OnDomainClickListener, OnConfirmListener {

	private static final String KEY_DATA = "domain-data";

	private DomainAction domainAction;
	private DomainLoader domainLoader;

	private DomainAdapter adapter;

	private AsyncCallback<DomainLoader.Result> domainLoad = this::onDomainLoaded;
	private AsyncCallback<DomainAction.Result> domainResult = this::onDomainResult;

	private String selectedDomain = "";


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		domainAction = new DomainAction(requireContext());
		domainLoader = new DomainLoader(requireContext());
		adapter = new DomainAdapter(this);
		setAdapter(adapter, false);

		if (savedInstanceState != null) {
			Object data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof Domains) {
				adapter.setItems((Domains) data);
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
			load(DomainAdapter.NO_INDEX, 0L);
			setRefresh(true);
		}
	}


	@Override
	public void onDestroy() {
		domainAction.cancel();
		domainLoader.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(DomainAdapter.NO_INDEX, 0L);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		domainLoader = new DomainLoader(requireContext());
		domainAction = new DomainAction(requireContext());
		load(DomainAdapter.NO_INDEX, 0L);
		setRefresh(true);
	}


	@Override
	public void onDomainRemove(String domain) {
		if (!isRefreshing() && domainAction.isIdle()) {
			if (ConfirmDialog.show(this, ConfirmDialog.DOMAIN_BLOCK_REMOVE, null)) {
				selectedDomain = domain;
			}
		}
	}


	@Override
	public boolean onPlaceholderClick(int index, long cursor) {
		if (!isRefreshing() && domainLoader.isIdle()) {
			load(index, cursor);
			return true;
		}
		return false;
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.DOMAIN_BLOCK_REMOVE) {
			DomainAction.Param param = new DomainAction.Param(DomainAction.Param.UNBLOCK, selectedDomain);
			domainAction.execute(param, domainResult);
		}
	}

	/**
	 * load domain list
	 *
	 * @param index  index where to insert domains into the list
	 * @param cursor cursor used to page through results
	 */
	private void load(int index, long cursor) {
		DomainLoader.Param param = new DomainLoader.Param(cursor, index);
		domainLoader.execute(param, domainLoad);
	}

	/**
	 *
	 */
	private void onDomainLoaded(DomainLoader.Result result) {
		setRefresh(false);
		if (result.domains != null) {
			adapter.addItems(result.domains, result.index);
		} else {
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}

	/**
	 *
	 */
	private void onDomainResult(DomainAction.Result result) {
		if (result.action == DomainAction.Result.UNBLOCK) {
			Context context = getContext();
			if (result.domain != null && context != null) {
				adapter.removeItem(result.domain);
				Toast.makeText(context, R.string.info_domain_removed, Toast.LENGTH_SHORT).show();
			}
		} else if (result.action == DomainAction.Result.ERROR) {
			adapter.disableLoading();
			Context context = getContext();
			if (context != null) {
				ErrorUtils.showErrorMessage(context, result.exception);
			}
		}
	}
}