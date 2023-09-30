package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.AppDatabase;

/**
 * @author nuclearfog
 */
public class AccountAction extends AsyncExecutor<AccountAction.Param, AccountAction.Result> {

	private AppDatabase db;

	/**
	 *
	 */
	public AccountAction(Context context) {
		db = new AppDatabase(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		db.removeLogin(param.id);
		return new Result(param.id);
	}

	/**
	 *
	 */
	public static class Param {

		final long id;

		public Param(long id) {
			this.id = id;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public final long id;

		Result(long id) {
			this.id = id;
		}
	}
}