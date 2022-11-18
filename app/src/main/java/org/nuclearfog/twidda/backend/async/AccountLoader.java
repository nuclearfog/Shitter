package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import org.nuclearfog.twidda.database.AccountDatabase;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.ui.fragments.AccountFragment;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * backend loader to get login information of local accounts
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncTask<Long, Void, List<Account>> {

	/**
	 * load all saved logins
	 */
	public static final int MODE_LOAD = 1;

	/**
	 * delete specific login
	 */
	public static final int MODE_DELETE = 2;

	private AccountDatabase accountDatabase;
	private WeakReference<AccountFragment> weakRef;

	private int mode;
	private long deleteId;

	/**
	 * @param mode action to take {@link #MODE_LOAD,#MODE_DELETE}
	 */
	public AccountLoader(AccountFragment fragment, int mode) {
		super();
		weakRef = new WeakReference<>(fragment);
		accountDatabase = new AccountDatabase(fragment.requireContext());
		this.mode = mode;
	}


	@Override
	protected List<Account> doInBackground(Long... param) {
		// get all logins
		if (mode == MODE_LOAD) {
			return accountDatabase.getLogins();
		}
		// delete login
		else if (mode == MODE_DELETE) {
			accountDatabase.removeLogin(param[0]);
			deleteId = param[0];
		}
		return null;
	}


	@Override
	protected void onPostExecute(List<Account> accounts) {
		AccountFragment fragment = weakRef.get();
		if (fragment != null) {
			if (mode == MODE_LOAD) {
				fragment.onSuccess(accounts);
			} else if (mode == MODE_DELETE) {
				fragment.onDelete(deleteId);
			}
		}
	}
}