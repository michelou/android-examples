package com.manning.aip.mymoviesdatabase.data

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object CategoryTable {

  final val TABLE_NAME = "category"

  object CategoryColumns extends BaseColumns {
    final val NAME = "name"
  }

  def onCreate(db: SQLiteDatabase) {
    val sb = new StringBuilder()

    // category table
    sb append "CREATE TABLE " append CategoryTable.TABLE_NAME append " ("
    sb append BaseColumns._ID append " INTEGER PRIMARY KEY, "
    sb append CategoryColumns.NAME append " TEXT UNIQUE NOT NULL"
    sb append ");"
    db execSQL sb.toString
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.TABLE_NAME)
    CategoryTable.onCreate(db)
  }

}
