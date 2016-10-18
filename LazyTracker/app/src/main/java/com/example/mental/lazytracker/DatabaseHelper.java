package com.example.mental.lazytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "myDatabase.db";
    public static final String TABLE_NAME = "Location_t";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LONGT = "longtitude";
    public static final String LAT = "latitude";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+NAME+" TEXT, "+LONGT+" DOUBLE, "+LAT+" DOUBLE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public boolean insertData(String name, double longt, double lat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, name);
        contentValues.put(LONGT, longt);
        contentValues.put(LAT, lat);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1) {
            return false;
        }else {
            return true;
        }
    }

    public void deleteTableData(SQLiteDatabase db) {
        SQLiteStatement s = db.compileStatement("select count(*) from " + TABLE_NAME);
        long count = s.simpleQueryForLong();
        if(count > 0) {
            db.execSQL("DELETE FROM " + TABLE_NAME);
        }


    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;


    }
}
