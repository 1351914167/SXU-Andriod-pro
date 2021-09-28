package com.zsh.sight.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.zsh.sight.adapter.Need;

import java.util.ArrayList;
import java.util.List;

public class NeedDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    private int idMax;
    public static final String CREATE_NEEDS = "create table Need("
            + "id integer primary key autoincrement, "
            + "account integer, "
            + "name text, "
            + "time text, "
            + "contend text, "
            + "mark integer) ";

    public NeedDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_NEEDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /*public List<Need> readNeeds() {
        List<Need> needList = new ArrayList<>();
        int id, account, mark;
        String name, time, contend;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("Need", null, null, null, null, null,"time");
        if (cursor.moveToFirst()) {
            do{
                id = cursor.getInt(cursor.getColumnIndex("id"));
                account = cursor.getInt(cursor.getColumnIndex("account"));
                mark = cursor.getInt(cursor.getColumnIndex("mark"));
                name = cursor.getString(cursor.getColumnIndex("name"));
                time = cursor.getString(cursor.getColumnIndex("time"));
                contend = cursor.getString(cursor.getColumnIndex("contend"));
                needList.add(need);
                if(id > idMax){
                    idMax = id;
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return needList;
    }*/

    /*public void changeNeed(Need need){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", need.getId());
        values.put("account", need.getAccount());
        values.put("name", need.getName());
        values.put("time", need.getTime());
        values.put("contend", need.getContend());
        values.put("mark", need.getMark());
        db.update("Need", values, "id=? and account=?",new String[]{""+need.getId(), ""+need.getAccount()});
        db.close();
    }

    public void insertNeed(Need need) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
//        values.put("id", need.getId());
        values.put("account", need.getAccount());
        values.put("name", need.getName());
        values.put("time", need.getTime());
        values.put("contend", need.getContend());
        values.put("mark", need.getMark());
        db.insert("Need", null, values);
    }*/
}
