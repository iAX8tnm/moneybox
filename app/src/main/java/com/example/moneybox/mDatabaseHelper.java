package com.example.moneybox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yuanc on 2018/3/21.
 */

public class mDatabaseHelper extends SQLiteOpenHelper {


    public static final String CREATE_DEPOSIT = "Create table if not exists Deposit ("
            + "id integer primary key autoincrement, "
            + "updateDate text, "
            + "updateTime text, "
            + "value integer)";

    public static final String CREATE_DAILY_DEPOSIT = "Create table if not exists DailyDeposit ("
            + "id integer primary key autoincrement, "
            + "updateDate text, "
            + "value real)";

    private Context mContext;

    public mDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEPOSIT);
        db.execSQL(CREATE_DAILY_DEPOSIT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Deposit");
        db.execSQL("drop table if exists DailyDeposit");
        onCreate(db);
    }
}
