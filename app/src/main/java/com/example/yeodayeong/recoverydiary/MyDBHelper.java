package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHelper extends SQLiteOpenHelper {
    public MyDBHelper( Context context) {
        super(context, "RecoveryDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table recoveryTBL (dateid TEXT,datetxt TEXT,timetxt TEXT,menu TEXT,type TEXT,remove INTEGER,removetxt TEXT,location TEXT,people TEXT,think TEXT,menuphoto BLOB,diaryindex INTEGER PRIMARY KEY AUTOINCREMENT);");
        db.execSQL("create table challangeTBL (challangeindex INTEGER PRIMARY KEY AUTOINCREMENT,challangeid INTEGER,challangeendid INTEGER,targetdate TEXT,daterange INTEGER,target TEXT,reward TEXT,targettype TEXT,targetnum INTEGER,targetupdown TEXT,resultnum TEXT); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recoveryTBL");
        db.execSQL("DROP TABLE IF EXISTS challangeTBL");

        onCreate(db);
    }

}
