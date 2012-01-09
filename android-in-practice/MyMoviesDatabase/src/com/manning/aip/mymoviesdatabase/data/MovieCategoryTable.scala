package com.manning.aip.mymoviesdatabase.data

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object MovieCategoryTable {

  final val TABLE_NAME = "movie_category"

  object MovieCategoryColumns {
    final val MOVIE_ID = "movie_id"
    final val CATEGORY_ID = "category_id"
  }

  def onCreate(db: SQLiteDatabase) {
    val sb = new StringBuilder()

    // movie_category mapping table
    sb append "CREATE TABLE " append MovieCategoryTable.TABLE_NAME append " ("

    sb append MovieCategoryColumns.MOVIE_ID append " INTEGER NOT NULL, "
    sb append MovieCategoryColumns.CATEGORY_ID append " INTEGER NOT NULL, "
    sb append "FOREIGN KEY(" append MovieCategoryColumns.MOVIE_ID append
              ") REFERENCES " append MovieTable.TABLE_NAME append
              "(" append BaseColumns._ID append "), "
    sb append "FOREIGN KEY(" append MovieCategoryColumns.CATEGORY_ID append
              ") REFERENCES " append CategoryTable.TABLE_NAME append
              "(" append BaseColumns._ID append ") , "
    sb append "PRIMARY KEY ( " append MovieCategoryColumns.MOVIE_ID append
              ", " append MovieCategoryColumns.CATEGORY_ID append ")"
    sb append ");"
    db execSQL sb.toString
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS " + MovieCategoryTable.TABLE_NAME)
    MovieCategoryTable.onCreate(db)
  }
}
