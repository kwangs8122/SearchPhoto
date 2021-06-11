package com.example.searchphoto.common

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val sql: String = "CREATE TABLE if not exists tb_notifications (" +
                "idx integer primary key autoincrement" +
                ", msg_id varchar(50)" +
                ", payload varchar(250)" +
                ");"

        db!!.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val sql: String = "DROP TABLE tb_notifications"

        db!!.execSQL(sql)
        onCreate(db)
    }
}