package com.example.peopleloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class to aid creation of SQLite database to hold people.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "people.db";
    static final String TABLE = "people";
    static final String COLUMN_PERSON = "person";
    static final String COLUMN_BLURB = "blurb";
    static final String COLUMN_PICTURE_LOC = "picture";
    private Context mContext = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mContext.getString(R.string.create_table_format,
                TABLE,
                COLUMN_PERSON,
                COLUMN_BLURB,
                COLUMN_PICTURE_LOC));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Ignore, there will only be one version of the schema
    }

}
