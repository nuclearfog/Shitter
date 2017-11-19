package org.nuclearfog.twidda.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.engine.TweetDatabase;


public class AppDatabase extends SQLiteOpenHelper
{
    private static AppDatabase mData;
    private Context context;

    private AppDatabase(Context context) {
        super(context, "twitter",null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String uQuery = context.getString(R.string.user_table);
        String tQuery = context.getString(R.string.tweet_table);
        db.execSQL(uQuery);
        db.execSQL(tQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + "user");
            db.execSQL("DROP TABLE IF EXISTS " + "tweet");
            onCreate(db);
        }
    }

    /**
     *  Store Home-Timeline
     */
    public void commit(TweetDatabase mTweet) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();

        for(int pos=0; pos<mTweet.getSize();pos++) {
           // values.put();


        }


        db.endTransaction();





    }


    public TweetDatabase read(){
        TweetDatabase mTweet = new TweetDatabase();



        return mTweet;
    }




    /**
     * Singleton Method
     * @param context Application Context
     * @return mData Object of this class
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (mData == null) {
            mData = new AppDatabase(context.getApplicationContext());
        }
        return mData;
    }
}
