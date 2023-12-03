package org.nuclearfog.twidda.backend.async;

import android.content.Context;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.config.GlobalSettings;
import org.nuclearfog.twidda.database.AppDatabase;
import org.nuclearfog.twidda.model.Account;
import org.nuclearfog.twidda.model.WebPush;

/**
 * Async class used to select or remove a login
 *
 * @author nuclearfog
 */
public class AccountAction extends AsyncExecutor<AccountAction.Param, AccountAction.Result> {

	private AppDatabase database;
	private GlobalSettings settings;

	/**
	 *
	 */
	public AccountAction(Context context) {
		database = new AppDatabase(context);
		settings = GlobalSettings.get(context);
	}


	@Override
	protected Result doInBackground(@NonNull Param param) {
		switch (param.mode) {
			case Param.SELECT:
				WebPush webPush = database.getWebPush(param.account);
				settings.setLogin(param.account, true);
				if (webPush != null) {
					settings.setPushEnabled(true);
					settings.setWebPush(webPush);
				} else {
					settings.setPushEnabled(false);
				}
				return new Result(Result.SELECT, param.account);

			case Param.REMOVE:
				database.removeLogin(param.account);
				return new Result(Result.REMOVE, param.account);
		}
		return null;
	}

	/**
	 *
	 */
	public static class Param {

		public static final int SELECT = 1;
		public static final int REMOVE = 2;

		final Account account;
		final int mode;

		public Param(int mode, Account account) {
			this.mode = mode;
			this.account = account;
		}
	}

	/**
	 *
	 */
	public static class Result {

		public static final int SELECT = 10;
		public static final int REMOVE = 20;

		public final Account account;
		public final int mode;

		Result(int mode, Account account) {
			this.mode = mode;
			this.account = account;
		}
	}
}