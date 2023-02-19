package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.backend.utils.AsyncExecutor;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Account;
import java.util.List;

/**
 * backend loader to get login information of local accounts
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncExecutor<AccountLoader.AccountParameter, AccountLoader.AccountResult> {

	/**
	 * load all saved logins
	 */
	public static final int MODE_LOAD = 1;

	/**
	 * delete specific login
	 */
	public static final int MODE_DELETE = 2;

	private AppDatabase db;


	public AccountLoader(Context context) {
		db = new AppDatabase(context);
	}


	@NonNull
	@Override
	protected AccountResult doInBackground(AccountParameter request) {
		try {
			switch (request.mode) {
				case MODE_LOAD:
					List<Account> accounts = db.getLogins();
					return new AccountResult(request.mode, 0L, accounts);

				case MODE_DELETE:
					db.removeLogin(request.id);
					return new AccountResult(request.mode, request.id, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new AccountResult(request.mode, 0L, null);
	}


	public static class AccountParameter {

		public final int mode;
		public final long id;

		public AccountParameter(int mode, long id) {
			this.mode = mode;
			this.id = id;
		}
	}


	public static class AccountResult {

		@Nullable
		public final List<Account> accounts;
		public final int mode;
		public final long id;

		AccountResult(int mode, long id, @Nullable List<Account> accounts) {
			this.accounts = accounts;
			this.mode = mode;
			this.id = id;
		}
	}
}