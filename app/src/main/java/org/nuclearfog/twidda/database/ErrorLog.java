package org.nuclearfog.twidda.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ErrorLog {
    private final AppDatabase dataHelper;

    public ErrorLog(Context context) {
        dataHelper = AppDatabase.getInstance(context);
    }

    public void add(String message) {
        SQLiteDatabase mData = dataHelper.getWritableDatabase();
        ContentValues item = new ContentValues();
        long time = new Date().getTime();
        item.put("time", time);
        item.put("message", message);
        mData.insertWithOnConflict("error",null,item,SQLiteDatabase.CONFLICT_IGNORE);
        mData.close();

    }

    public void remove(long time) {
        SQLiteDatabase mData = dataHelper.getWritableDatabase();
        mData.delete("error", "time = "+time, null);
        mData.close();
    }

    public List<String> getErrorList() {
        SQLiteDatabase mData = dataHelper.getReadableDatabase();
        List<String> list = new ArrayList<>();
        int limit = 0;
        String query = "SELECT * FROM error ORDER BY time DESC";
        Cursor cursor = mData.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("time");
                long time = cursor.getLong(index);
                index = cursor.getColumnIndex("message");
                String message = cursor.getString(index);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY);
                String dateString = sdf.format(new Date(time));
                list.add(dateString+" : "+message);

            } while(cursor.moveToNext() && limit++ < 100);
        }
        cursor.close();
        mData.close();
        return list;
    }
}