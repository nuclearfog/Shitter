package org.nuclearfog.twidda.database;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;
import org.nuclearfog.twidda.database.impl.AccountImpl;
import org.nuclearfog.twidda.model.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * this database stores multi user logins
 *
 * @author nuclearfog
 */
public class AccountDatabase {

	/**
	 * selection for account entry
	 */
	private static final String ACCOUNT_SELECTION = AccountTable.ID + "=?";

	/**
	 * default sort order of the entries
	 * sort by date of creation, starting with the latest entry
	 */
	private static final String SORT_BY_CREATION = AccountTable.DATE + " DESC";


	private DatabaseAdapter dataHelper;
	private AppDatabase database;

	/**
	 * @param context current activity context
	 */
	public AccountDatabase(Context context) {
		dataHelper = DatabaseAdapter.getInstance(context);
		database = new AppDatabase(context);
	}

	/**
	 * register user login
	 *
	 * @param account Account information
	 */
	public void saveLogin(Account account) {
		ContentValues values = new ContentValues();

		values.put(AccountTable.ID, account.getId());
		values.put(AccountTable.ACCESS_TOKEN, account.getOauthToken());
		values.put(AccountTable.TOKEN_SECRET, account.getOauthSecret());
		values.put(AccountTable.DATE, account.getLoginDate());
		values.put(AccountTable.HOSTNAME, account.getHostname());
		values.put(AccountTable.CLIENT_ID, account.getConsumerToken());
		values.put(AccountTable.CLIENT_SECRET, account.getConsumerSecret());
		values.put(AccountTable.API, account.getApiType());

		SQLiteDatabase db = dataHelper.getDatabase();
		db.beginTransaction();
		db.insertWithOnConflict(AccountTable.NAME, "", values, CONFLICT_REPLACE);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/**
	 * get all user logins
	 *
	 * @return list of all logins
	 */
	public List<Account> getLogins() {
		ArrayList<Account> result = new ArrayList<>();

		SQLiteDatabase db = dataHelper.getDatabase();
		Cursor cursor = db.query(AccountTable.NAME, AccountImpl.COLUMNS, null, null, null, null, SORT_BY_CREATION);
		if (cursor.moveToFirst()) {
			result.ensureCapacity(cursor.getCount());
			do
			{
				AccountImpl account = new AccountImpl(cursor);
				account.addUser(database.getUser(account.getId()));
				result.add(account);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	/**
	 * remove login information from storage
	 *
	 * @param id account ID to remove
	 */
	public void removeLogin(long id) {
		String[] args = {Long.toString(id)};

		SQLiteDatabase db = dataHelper.getDatabase();
		db.delete(AccountTable.NAME, ACCOUNT_SELECTION, args);
	}
}