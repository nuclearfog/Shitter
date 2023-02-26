package org.nuclearfog.twidda.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.async.AccountLoader;
import org.nuclearfog.twidda.backend.async.AccountLoader.AccountParameter;
import org.nuclearfog.twidda.backend.async.AccountLoader.AccountResult;
import org.nuclearfog.twidda.backend.async.AsyncExecutor.AsyncCallback;
import org.nuclearfog.twidda.backend.async.DatabaseAction;
import org.nuclearfog.twidda.backend.async.DatabaseAction.DatabaseParam;
import org.nuclearfog.twidda.backend.async.DatabaseAction.DatabaseResult;
import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.activities.AccountActivity;
import org.nuclearfog.twidda.ui.adapter.AccountAdapter;
import org.nuclearfog.twidda.ui.adapter.AccountAdapter.OnAccountClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * fragment class to show registered accounts
 *
 * @author nuclearfog
 */
public class AccountFragment extends ListFragment implements OnAccountClickListener, OnConfirmListener, AsyncCallback<AccountResult> {

	private AccountLoader loginTask;
	private DatabaseAction databaseAsync;
	private GlobalSettings settings;
	private AccountAdapter adapter;
	private ConfirmDialog dialog;

	private long selectedId;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		dialog = new ConfirmDialog(requireContext());
		settings = GlobalSettings.getInstance(requireContext());
		adapter = new AccountAdapter(requireContext(), this);
		loginTask = new AccountLoader(requireContext());
		databaseAsync = new DatabaseAction(requireContext());

		setAdapter(adapter);
		dialog.setConfirmListener(this);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (adapter.isEmpty()) {
			setRefresh(true);
			load(AccountParameter.LOAD);
		}
	}


	@Override
	public void onDestroy() {
		loginTask.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(AccountParameter.LOAD);
	}


	@Override
	protected void onReset() {
		setRefresh(true);
		load(AccountParameter.LOAD);
	}


	@Override
	public void onAccountClick(Account account) {
		settings.setLogin(account, true);
		databaseAsync.execute(new DatabaseParam(DatabaseParam.DELETE), this::onDatabaseResult);
		if (account.getUser() != null) {
			String message = getString(R.string.info_account_selected, account.getUser().getScreenname());
			Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public void onAccountRemove(Account account) {
		if (!dialog.isShowing()) {
			selectedId = account.getId();
			dialog.show(ConfirmDialog.REMOVE_ACCOUNT);
		}
	}


	@Override
	public void onConfirm(int type, boolean rememberChoice) {
		if (type == ConfirmDialog.REMOVE_ACCOUNT) {
			load(AccountParameter.DELETE);
		}
	}


	@Override
	public void onResult(AccountResult result) {
		setRefresh(false);
		switch (result.mode) {
			case AccountResult.LOAD:
				if (result.accounts != null) {
					adapter.replaceItems(result.accounts);
				}
				break;

			case AccountResult.DELETE:
				if (result.id != 0)
					adapter.removeItem(result.id);
				break;

			case AccountResult.ERROR:
				if (getContext() != null)
					Toast.makeText(getContext(), R.string.error_acc_loading, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	/**
	 * called from {@link DatabaseAction} when all data of the previous login were removed
	 */
	public void onDatabaseResult(DatabaseResult result) {
		// finish activity and return to parent activity
		requireActivity().setResult(AccountActivity.RETURN_ACCOUNT_CHANGED);
		requireActivity().finish();
	}

	/**
	 *
	 */
	public void load(int mode) {
		AccountParameter request = new AccountParameter(mode, selectedId);
		loginTask.execute(request, this);
	}
}