/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.events

import android.provider.BaseColumns._ID;
import org.example.events.Constants._
import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}

import EventsData._  // companion object

class EventsData(ctx: Context)
extends SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

  override def onCreate(db: SQLiteDatabase) {
    db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TIME
            + " INTEGER," + TITLE + " TEXT NOT NULL);")
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
    onCreate(db)
  }

}

object EventsData {
  private final val DATABASE_NAME = "events.db"
  private final val DATABASE_VERSION = 1
}
