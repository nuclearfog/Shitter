package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.nuclearfog.twidda.database.DatabaseAdapter.UserExcludeTable;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This database class provides methods to load and store IDs of unwanted users.
 * Blocked users and their tweets will be excluded from Twitter search if enabled.
 *
 * @author nuclearfog
 */
public class FilterDatabase {

    /**
     * selection to get the exclude list of the current user
     */
    private static final String LIST_SELECT = UserExcludeTable.OWNER + "=?";

    /**
     * selection to get a column
     */
    private static final String COLUMN_SELECT = LIST_SELECT + " AND " + UserExcludeTable.ID + "=?";

    /**
     * column to fetch from the database
     */
    private static final String[] LIST_ID_COL = {UserExcludeTable.ID};


    private DatabaseAdapter dataHelper;
    private GlobalSettings settings;

    /**
     * @param context current context
     */
    public FilterDatabase(Context context) {
        dataHelper = DatabaseAdapter.getInstance(context);
        settings = GlobalSettings.getInstance(context);
    }

    /**
     * replace exclude list with a new version
     *
     * @param ids list of user IDs
     */
    public void setExcludeList(List<Long> ids) {
        long homeId = settings.getCurrentUserId();
        String[] args = {Long.toString(homeId)};
        SQLiteDatabase db = getDbWrite();

        db.delete(UserExcludeTable.NAME, LIST_SELECT, args);
        for (long id : ids) {
            ContentValues column = new ContentValues(2);
            column.put(UserExcludeTable.ID, id);
            column.put(UserExcludeTable.OWNER, homeId);
            db.insert(UserExcludeTable.NAME, null, column);
        }
        commit(db);
    }

    /**
     * return current users exclude set containing user IDs
     *
     * @return a set of user IDs
     */
    public Set<Long> getExcludeSet() {
        String[] args = {Long.toString(settings.getCurrentUserId())};
        SQLiteDatabase db = getDbRead();
        Cursor cursor = db.query(UserExcludeTable.NAME, LIST_ID_COL, LIST_SELECT, args, null, null, null, null);

        Set<Long> result = new TreeSet<>();
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                result.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    /**
     * add user to the exclude database
     *
     * @param userId ID of the user
     */
    public void addUser(long userId) {
        SQLiteDatabase db = getDbWrite();
        ContentValues column = new ContentValues(2);
        column.put(UserExcludeTable.ID, userId);
        column.put(UserExcludeTable.OWNER, settings.getCurrentUserId());
        db.insert(UserExcludeTable.NAME, null, column);
        commit(db);
    }

    /**
     * remove user from the exclude database
     *
     * @param userId ID of the user
     */
    public void removeUser(long userId) {
        String[] args = {Long.toString(settings.getCurrentUserId()), Long.toString(userId)};
        SQLiteDatabase db = getDbWrite();
        db.delete(UserExcludeTable.NAME, COLUMN_SELECT, args);
        commit(db);
    }

    /**
     * Get SQLite instance for reading database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbRead() {
        return dataHelper.getDatabase();
    }

    /**
     * GET SQLite instance for writing database
     *
     * @return SQLite instance
     */
    private synchronized SQLiteDatabase getDbWrite() {
        SQLiteDatabase db = dataHelper.getDatabase();
        db.beginTransaction();
        return db;
    }

    /**
     * Commit changes and close Database
     *
     * @param db database instance
     */
    private synchronized void commit(SQLiteDatabase db) {
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}