package com.example.chatrobot01;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteConsole {

    private MySQLite sqLite;

    public SQLiteConsole(Context context){
        sqLite = new MySQLite(context);
    }

    public void insert(MyDataSet dataSet){
        String place = dataSet.getPlace();
        String text = dataSet.getData();
        SQLiteDatabase db = sqLite.getWritableDatabase();//获取数据库对象
        ContentValues values = new ContentValues();//创建装载数据的类
        values.put("place",place);//放入数据
        values.put("text",text);
        db.insert("data",null,values);//往data表插入数据
        db.close();//关闭数据库
    }

    public List<MyDataSet> queryAll(){
        List<MyDataSet> dataList = new ArrayList<MyDataSet>();
        SQLiteDatabase db = sqLite.getReadableDatabase();
        //query最后一个参数代表着排序
        Cursor cursor = db.query("data",null,null,null,null,null,"id ASC");
        while(cursor.moveToNext()){
            String place = cursor.getString(1);//获取数据库中的文本数据
            String text = cursor.getString(2);
            dataList.add(new MyDataSet(place,text));
        }
        cursor.close();
        db.close();
        return dataList;
    }

}
