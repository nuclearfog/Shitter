package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.lists.Accounts;

/**
 * Async loader to get saved login informations
 *
 * @author nuclearfog
 */
public class AccountLoader extends AsyncExecutor<Void, AccountLoader.Result> {

	private AppDatabase db;

	/**
	 *
	 */
	public AccountLoader(Context context) {
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Void v) {
		return new Result(db.getLogins());
	}

	/**
	 *
	 */
	public static class Result {

		public final Accounts accounts;

		Result(Accounts accounts) {
			this.accounts = accounts;
		}
	}
}