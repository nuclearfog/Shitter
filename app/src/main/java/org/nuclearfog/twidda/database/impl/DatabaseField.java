package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.DatabaseAdapter.UserFieldTable;
import org.nuclearfog.twidda.model.Field;

/**
 * Database implementation of {@link Field}
 *
 * @author nuclearfog
 */
public class DatabaseField implements Field, UserFieldTable {

	private static final long serialVersionUID = 2756139865280400840L;

	/**
	 * database column projection
	 */
	public static final String[] PROJECTION = {
			UserFieldTable.KEY,
			UserFieldTable.VALUE,
			UserFieldTable.TIMESTAMP
	};

	private String key = "";
	private String value = "";
	private long timestamp;

	/**
	 * @param cursor database cursor using this {@link #PROJECTION}
	 */
	public DatabaseField(Cursor cursor) {
		String key = cursor.getString(0);
		String value = cursor.getString(1);
		timestamp = cursor.getLong(2);
		if (key != null)
			this.key = key;
		if (value != null)
			this.value = value;
	}


	@Override
	public String getKey() {
		return key;
	}


	@Override
	public String getValue() {
		return value;
	}


	@Override
	public long getTimestamp() {
		return timestamp;
	}


	@NonNull
	@Override
	public String toString() {
		return "key=\"" + key + "\" value=\"" + value + "\"";
	}
}