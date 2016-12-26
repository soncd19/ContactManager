package com.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cao Dinh Son on 11/16/2016.
 */

public class BlockCallDataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "block_call.db";

    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BLACKLIST = "blacklist";

    public static final String _ID = "id";

    public static final String PHONE_NUMBER = "phone_number";

    private static final String TABLE_CREATE = "create table " + TABLE_BLACKLIST
            + "( " + _ID + " integer primary key autoincrement, " + PHONE_NUMBER + " text not null);";

    public BlockCallDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
