package org.nuclearfog.twidda.database;

import android.database.Cursor;

import org.nuclearfog.twidda.model.Trend;

class TrendDB implements Trend {

    private String name;
    private int range;
    private int rank;

    TrendDB(Cursor cursor) {
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
}