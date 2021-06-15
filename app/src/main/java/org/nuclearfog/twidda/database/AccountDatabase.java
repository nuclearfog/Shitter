package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.backend.model.Account;
import org.nuclearfog.twidda.database.DatabaseAdapter.AccountTable;

import java.util.ArrayList;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/**
 * this database stores multi user logins
 *
 * @author nuclearfog
 */
public class AccountDatabase {

    /**
     * projection of the columns with fixed order
     */
    private static final String[] PROJECTION = {
            AccountTable.ID,
            AccountTable.KEY1,
            AccountTable.KEY2,
            AccountTable.DATE
    };

    /**
     * selection for account entry
     */
    private static final String ACCOUNT_SELECTION = AccountTable.ID + "=?";

    /**
     * default sort order of the entries
     * sort by date of creation, starting with the latest entry
     */
    private static final String SORT_BY_CREATION = AccountTable.DATE + " DESC";

    /**
     * singleton instance
     */
    private static final AccountDatabase INSTANCE = new AccountDatabase();


    private DatabaseAdapter dataHelper;


    private AccountDatabase() {
    }

    /**
     * get singleton instance
     *
     * @return instance
     */
    public static AccountDatabase getInstance(Context context) {
        if (INSTANCE.dataHelper == null)
            INSTANCE.dataHelper = DatabaseAdapter.getInstance(context.getApplicationContext());
        return INSTANCE;
    }

    /**
     * register user login
     *
     * @param id   User ID
     * @param key1 access token 1
     * @param key2 access token 2
     */
    public void setLogin(long id, String key1, String key2) {
        ContentValues values = new ContentValues(4);

        values.put(AccountTable.ID, id);
        values.put(AccountTable.KEY1, key1);
        values.put(AccountTable.KEY2, key2);
        values.put(AccountTable.DATE, System.currentTimeMillis());

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
        Cursor cursor = db.query(AccountTable.NAME, PROJECTION, null, null, null, null, SORT_BY_CREATION);
        if (cursor.moveToFirst()) {
            result.ensureCapacity(cursor.getCount());
            do {
                long id = cursor.getLong(0);
                String key1 = cursor.getString(1);
                String key2 = cursor.getString(2);
                long date = cursor.getLong(3);
                Account account = new Account(id, date, key1, key2);
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

    /**
     * check if user exists
     *
     * @param id User ID
     * @return true if user was found
     */
    public boolean exists(long id) {
        String[] args = {Long.toString(id)};
        SQLiteDatabase db = dataHelper.getDatabase();
        Cursor cursor = db.query(AccountTable.NAME, null, ACCOUNT_SELECTION, args, null, null, null, "1");
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }
}