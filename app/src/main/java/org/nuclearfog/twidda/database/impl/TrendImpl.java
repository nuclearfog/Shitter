package org.nuclearfog.twidda.database.impl;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.model.Trend;

/**
 * database implementation for a trend
 *
 * @author nuclearfog
 */
public class TrendImpl implements Trend {

	private static final long serialVersionUID = 1799880502954768985L;

	/**
	 * SQLite columns
	 */
	public static final String[] COLUMNS = {
			DatabaseAdapter.TrendTable.TREND,
			DatabaseAdapter.TrendTable.VOL,
			DatabaseAdapter.TrendTable.INDEX,
			DatabaseAdapter.TrendTable.ID
	};

	private String name;
	private int range;
	private int rank;
	private long id;

	/**
	 * @param cursor database cursor using this {@link #COLUMNS} projection
	 */
	public TrendImpl(Cursor cursor) {
		name = cursor.getString(0);
		range = cursor.getInt(1);
		rank = cursor.getInt(2);
		id = cursor.getLong(3);
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public long getLocationId() {
		return id;
	}


	@Override
	public int getRank() {
		return rank;
	}


	@Override
	public int getPopularity() {
		return range;
	}


	@NonNull
	@Override
	public String toString() {
		return "rank=" + rank + " name=\"" + name + "\"";
	}
}