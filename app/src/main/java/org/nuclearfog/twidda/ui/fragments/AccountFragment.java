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
import org.nuclearfog.twidda.model.lists.Accounts;
import org.nuclearfog.twidda.notification.PushSubscription;
import org.nuclearfog.twidda.ui.activities.AccountActivity;
import org.nuclearfog.twidda.ui.adapter.AccountAdapter;
import org.nuclearfog.twidda.ui.adapter.AccountAdapter.OnAccountClickListener;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

import java.io.Serializable;

/**
 * fragment class to show registered accounts
 *
 * @author nuclearfog
 */
public class AccountFragment extends ListFragment implements OnAccountClickListener, OnConfirmListener, AsyncCallback<AccountResult> {

	/**
	 * internal Bundle key used to save adapter items
	 * value type is {@link Accounts}
	 */
	private static final String KEY_SAVE = "account-data";

	private AccountLoader accountLoader;
	private DatabaseAction databaseAction;
	private GlobalSettings settings;
	private AccountAdapter adapter;
	private ConfirmDialog dialog;

	private long selectedId;

	private AsyncCallback<DatabaseResult> databaseResult = this::onDatabaseResult;


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		dialog = new ConfirmDialog(requireActivity(), this);
		settings = GlobalSettings.get(requireContext());
		accountLoader = new AccountLoader(requireContext());
		databaseAction = new DatabaseAction(requireContext());
		adapter = new AccountAdapter(this);
		setAdapter(adapter);

		if (savedInstanceState != null) {
			Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
			if (data instanceof Accounts) {
				adapter.replaceItems((Accounts) data);
				return;
			}
		}
		load(AccountParameter.LOAD);
		setRefresh(true);
	}


	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(KEY_SAVE, adapter.getItems());
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onDestroy() {
		accountLoader.cancel();
		databaseAction.cancel();
		super.onDestroy();
	}


	@Override
	protected void onReload() {
		load(AccountParameter.LOAD);
	}


	@Override
	protected void onReset() {
		adapter.clear();
		setRefresh(true);
		load(AccountParameter.LOAD);
	}


	@Override
	public void onAccountClick(Account account) {
		settings.setLogin(account, true);
		databaseAction.execute(new DatabaseParam(DatabaseParam.DELETE), databaseResult);
		if (settings.pushEnabled()) {
			PushSubscription.subscripe(requireContext());
		}
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
	public void onConfirm(int type) {
		if (type == ConfirmDialog.REMOVE_ACCOUNT) {
			load(AccountParameter.DELETE);
		}
	}


	@Override
	public void onResult(@NonNull AccountResult result) {
		switch (result.mode) {
			case AccountResult.LOAD:
				if (result.accounts != null) {
					adapter.replaceItems(result.accounts);
				}
				break;

			case AccountResult.DELETE:
				adapter.removeItem(result.id);
				break;

			case AccountResult.ERROR:
				if (getContext() != null)
					Toast.makeText(getContext(), R.string.error_acc_loading, Toast.LENGTH_SHORT).show();
				break;
		}
		setRefresh(false);
	}

	/**
	 * called from {@link DatabaseAction} when all data of the previous login were removed
	 */
	@SuppressWarnings("unused")
	private void onDatabaseResult(DatabaseResult result) {
		// finish activity and return to parent activity
		requireActivity().setResult(AccountActivity.RETURN_ACCOUNT_CHANGED);
		requireActivity().finish();
	}

	/**
	 *
	 */
	private void load(int mode) {
		AccountParameter request = new AccountParameter(mode, selectedId);
		accountLoader.execute(request, this);
	}
}