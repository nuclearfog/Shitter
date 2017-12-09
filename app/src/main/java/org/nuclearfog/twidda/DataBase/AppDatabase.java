package org.nuclearfog.twidda.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.nuclearfog.twidda.R;

public class AppDatabase extends SQLiteOpenHelper
{
    private static AppDatabase mData;
    private Context c;

    private AppDatabase(Context context) {
        super(context, "twitter.sqlite",null, 1);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String uQuery  = c.getString(R.string.user_table);
        String tQuery  = c.getString(R.string.tweet_table);
        String trQuery = c.getString(R.string.trend_table);
        db.execSQL(uQuery);
        db.execSQL(tQuery);
        db.execSQL(trQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "user");
        db.execSQL("DROP TABLE IF EXISTS " + "tweet");
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