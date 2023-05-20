package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DomainAction;
import org.nuclearfog.twidda.backend.async.DomainAction.DomainParam;
import org.nuclearfog.twidda.backend.async.DomainAction.DomainResult;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.lists.Domains;
import org.nuclearfog.twidda.ui.adapter.DomainAdapter;
import org.nuclearfog.twidda.ui.adapter.DomainAdapter.OnDomainClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.io.Serializable;

/**
 * Fragment containing a list of domain names
 *
 * @author nuclearfog
 */
public class DomainFragment extends ListFragment implements OnDomainClickListener, OnConfirmListener, AsyncCallback<DomainResult> {

	private static final String KEY_DATA = "domain-data";

	private DomainAction domainAction;
	private DomainAdapter adapter;
	private ConfirmDialog dialog;

	private String selectedDomain = "";


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		adapter = new DomainAdapter(requireContext(), this);
		domainAction = new DomainAction(requireContext());
		dialog = new ConfirmDialog(requireContext());
		setAdapter(adapter);
		dialog.setConfirmListener(this);

		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_DATA);
			if (data instanceof Domains) {
				adapter.replaceItems((Domains) data);
				return;
			}
		}
		load(DomainAdapter.NO_INDEX, DomainParam.NO_CURSOR);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_DATA, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		domainAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(DomainAdapter.NO_INDEX, DomainParam.NO_CURSOR);
	}


	@Override
	protected void onReset() {
		setRefresh(true);
		load(DomainAdapter.NO_INDEX, DomainParam.NO_CURSOR);
	}


	@Override
	public void onDomainRemove(String domain) {
		if (!isRefreshing() && !dialog.isShowing()) {
			dialog.show(ConfirmDialog.DOMAIN_BLOCK_REMOVE);
			selectedDomain = domain;
		}
	}


	@Override
	public boolean onPlaceholderClick(int index, long cursor) {
		if (!isRefreshing()) {
			load(index, cursor);
			return true;
		}
		return false;
	}


	@Override
	public void onResult(@NonNull DomainResult result) {
		setRefresh(false);
		if (result.mode == DomainResult.MODE_LOAD) {
			if (result.domains != null) {
				adapter.addItems(result.domains, result.index);
			}
		} else if (result.mode == DomainResult.MODE_UNBLOCK) {
			if (result.domain != null) {
				adapter.removeItem(result.domain);
				Toast.makeText(requireContext(), R.string.info_domain_removed, Toast.LENGTH_SHORT).show();
			}
		} else if (result.mode == DomainResult.ERROR) {
			String message = ErrorHandler.getErrorMessage(getContext(), result.exception);
			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			adapter.disableLoading();
		}
	}


	@Override
	public void onConfirm(int type) {
		if (type == ConfirmDialog.DOMAIN_BLOCK_REMOVE) {
			DomainParam param = new DomainParam(DomainParam.MODE_UNBLOCK, DomainAdapter.NO_INDEX, DomainParam.NO_CURSOR, selectedDomain);
			domainAction.execute(param, this);
		}
	}

	/**
	 * load domain list
	 *
	 * @param index index where to insert domains into the list
	 * @param cursor cursor used to page through results
	 */
	private void load(int index, long cursor) {
		DomainParam param = new DomainParam(DomainParam.MODE_LOAD, index, cursor, null);
		domainAction.execute(param, this);
	}
}