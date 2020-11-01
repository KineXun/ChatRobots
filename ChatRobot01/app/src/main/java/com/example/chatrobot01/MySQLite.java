package com.example.chatrobot01;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLite extends SQLiteOpenHelper {
    public MySQLite(Context context){
        super(context,"chatData.db",null,5);//建库
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table data(" +
                "id integer primary key autoincrement," +
                "place varchar(10),"+
                "text varchar(200))");//建表
    }

    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("alter table person add account varchar(20)");
    }
}
