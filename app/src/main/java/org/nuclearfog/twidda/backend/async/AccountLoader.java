package org.nuclearfog.twidda.backend.async;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.database.AppDatabase;
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

	private AppDatabase db;
	private WeakReference<AccountFragment> weakRef;

	private int mode;
	private long deleteId;

	/**
	 * @param mode action to take {@link #MODE_LOAD,#MODE_DELETE}
	 */
	public AccountLoader(AccountFragment fragment, int mode) {
		super();
		weakRef = new WeakReference<>(fragment);
		db = new AppDatabase(fragment.requireContext());
		this.mode = mode;
	}


	@Override
	protected List<Account> doInBackground(Long... param) {
		try {
			// get all logins
			if (mode == MODE_LOAD) {
				return db.getLogins();
			}
			// delete login
			else if (mode == MODE_DELETE) {
				db.removeLogin(param[0]);
				deleteId = param[0];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onPostExecute(@Nullable List<Account> accounts) {
		AccountFragment fragment = weakRef.get();
		if (fragment != null) {
			if (mode == MODE_LOAD) {
				if (accounts != null) {
					fragment.onSuccess(accounts);
				} else {
					fragment.onError();
				}
			} else if (mode == MODE_DELETE) {
				if (deleteId > 0) {
					fragment.onDelete(deleteId);
				} else {
					fragment.onError();
				}
			}
		}
	}
}