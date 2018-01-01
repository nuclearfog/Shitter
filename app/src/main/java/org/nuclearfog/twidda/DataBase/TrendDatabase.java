package org.nuclearfog.twidda.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Trend;
import twitter4j.Trends;

public class TrendDatabase {

    private AppDatabase dataHelper;
    private List<String> trendName;
    private List<String> trendLink;
    private List<Integer> trendpos;
    private Trends trends;
    private int size = 0;
    private Context c;

    public TrendDatabase(Trends trends, Context c) {
        this.trends = trends;
        this.c = c;
        init();
        setup();
        store();
    }

    public TrendDatabase(Context c) {
        this.c = c;
        init();
        load();
    }

    public String getTrendname(int pos){ return trendName.get(pos); }
    public String getTrendlink(int pos){return trendLink.get(pos);}
    public int getTrendpos(int pos){ return trendpos.get(pos); }

    public int getSize(){
        if(trendName != null)
            return trendName.size();
        else
            return size;
    }

    private void load() {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String SQL_TREND = "SELECT * FROM trend ORDER BY trendpos ASC";
        Cursor cursor = db.rawQuery(SQL_TREND,null);
        int index;
        if(cursor.moveToFirst()) {
            do {
                index = cursor.getColumnIndex("trendpos"); // trendpos
                trendpos.add(cursor.getInt(index));
                index = cursor.getColumnIndex("trendname"); // trendname
                trendName.add(cursor.getString(index));
                index = cursor.getColumnIndex("trendlink"); // trendlink
                trendLink.add(cursor.getString(index));
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    private void store() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues trend = new ContentValues();
        for(int pos = 0; pos < getSize(); pos++) {
            trend.put("trendpos", getTrendpos(pos));
            trend.put("trendname", getTrendname(pos));
            trend.put("trendlink", getTrendlink(pos));
            db.insertWithOnConflict("trend",null, trend,SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private void init() {
        dataHelper = AppDatabase.getInstance(c);
        trendpos  = new ArrayList<>();
        trendName = new ArrayList<>();
        trendLink = new ArrayList<>();
    }

    private void setup() {
        SharedPreferences settings = c.getSharedPreferences("settings", 0);
        SharedPreferences.Editor e = settings.edit();
        e.putString("location", trends.getLocation().getName()).apply();
        for(Trend trend : trends.getTrends()) {
            trendName.add(trend.getName());
            trendLink.add(trend.getURL());
            trendpos.add(++size);
        }
    }
}