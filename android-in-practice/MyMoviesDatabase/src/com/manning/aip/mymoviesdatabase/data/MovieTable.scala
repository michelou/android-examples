package com.manning.aip.mymoviesdatabase.data

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object MovieTable {

  final val TABLE_NAME = "movie"

  object MovieColumns {
    final val _ID = BaseColumns._ID
    final val _COUNT = BaseColumns._COUNT

    final val HOMEPAGE = "homepage"
    final val NAME = "movie_name"
    final val RATING = "rating"
    final val TAGLINE = "tagline"
    final val THUMB_URL = "thumb_url"
    final val IMAGE_URL = "image_url"
    final val TRAILER = "trailer"
    final val URL = "url"
    final val YEAR = "year"
  }

  def onCreate(db: SQLiteDatabase) {
    val sb = new StringBuilder()

    // movie table
    sb append "CREATE TABLE " append MovieTable.TABLE_NAME append " ("
    sb append BaseColumns._ID append " INTEGER PRIMARY KEY, "
    sb append MovieColumns.HOMEPAGE append " TEXT, "
    sb append MovieColumns.NAME append " TEXT UNIQUE NOT NULL, " // movie names aren't unique, but for simplification we constrain
    sb append MovieColumns.RATING append " INTEGER, "
    sb append MovieColumns.TAGLINE append " TEXT, "
    sb append MovieColumns.THUMB_URL append " TEXT, "
    sb append MovieColumns.IMAGE_URL append " TEXT, "
    sb append MovieColumns.TRAILER append " TEXT, "
    sb append MovieColumns.URL append " TEXT, "
    sb append MovieColumns.YEAR append " INTEGER"
    sb append ");"
    db execSQL sb.toString
  }

  def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS " + MovieTable.TABLE_NAME)
    MovieTable.onCreate(db)
  }
}
