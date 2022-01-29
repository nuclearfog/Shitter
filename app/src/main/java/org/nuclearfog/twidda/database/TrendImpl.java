package org.nuclearfog.twidda.database;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.model.Trend;

/**
 * database implementation for a trend
 *
 * @author nuclearfog
 */
class TrendImpl implements Trend {

    private String name;
    private int range;
    private int rank;


    TrendImpl(Cursor cursor) {
        name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.TrendTable.TREND));
        range = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.TrendTable.VOL));
        rank = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.TrendTable.INDEX));
    }

    @Override
    public String getName() {
        return name;
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
        return "rank:" + rank + " name:\"" + name + "\"";
    }
}