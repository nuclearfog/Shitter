package org.nuclearfog.twidda.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.nuclearfog.twidda.R;

public class AppDatabase extends SQLiteOpenHelper
{
    private static AppDatabase mData;
    private Context context;

    private AppDatabase(Context context) {
        super(context, context.getString(R.string.database),null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String uQuery  = context.getString(R.string.tableUser);
        String tQuery  = context.getString(R.string.tableTweet);
        String trQuery = context.getString(R.string.tableTrend);
        String hQuery  = context.getString(R.string.tableHome);
        String fQuery  = context.getString(R.string.tableFavorit);
        db.execSQL(uQuery);
        db.execSQL(tQuery);
        db.execSQL(trQuery);
        db.execSQL(hQuery);
        db.execSQL(fQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "user");
        db.execSQL("DROP TABLE IF EXISTS " + "tweet");
        db.execSQL("DROP TABLE IF EXISTS " + "favorit");
        db.execSQL("DROP TABLE IF EXISTS " + "timeline");
        db.execSQL("DROP TABLE IF EXISTS " + "trend");
        onCreate(db);
    }

    /**
     * Singleton Method
     * @param context Application Context
     * @return mData Object of this class
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (mData == null) {
            mData = new AppDatabase(context);
        }
        return mData;
    }
}