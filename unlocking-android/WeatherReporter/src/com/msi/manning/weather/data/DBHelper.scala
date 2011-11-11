package com.msi.manning.weather
package data

import android.content.{ContentValues, Context}
import android.database.{Cursor, SQLException}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log

import java.lang.{Integer => JInt, Long => JLong}

import scala.collection.mutable.ListBuffer

class DBHelper(context: Context) {
  import DBHelper._  // companion object

  private var db: SQLiteDatabase = _
  private val dbOpenHelper: DBOpenHelper = new DBOpenHelper(context)

  establishDb()

  private def establishDb() {
    if (db == null) db = dbOpenHelper.getWritableDatabase
  }

  def cleanup() {
    if (db != null) {
      db.close()
      db = null
    }
  }

  def insert(location: Location) {
    val values = new ContentValues()
    values.put("zip", location.zip)
    values.put("city", location.city)
    values.put("region", location.region)
    values.put("lastalert", JLong.valueOf(location.lastalert))
    values.put("alertenabled", JInt.valueOf(location.alertenabled))
    db.insert(DB_TABLE, null, values)
  }

  def update(location: Location) {
    val values = new ContentValues()
    values.put("zip", location.zip)
    values.put("city", location.city)
    values.put("region", location.region)
    values.put("lastalert", JLong.valueOf(location.lastalert))
    values.put("alertenabled", JInt.valueOf(location.alertenabled))
    db.update(DB_TABLE, values, "_id=" + location.id, null)
  }

  def delete(id: Long) {
    db.delete(DB_TABLE, "_id=" + id, null)
  }

  def delete(zip: String) {
    db.delete(DB_TABLE, "zip='" + zip + "'", null)
  }

  def get(zip: String): Location = {
    var c: Cursor = null
    var location: Location = null
    try {
      c = this.db.query(true, DB_TABLE, COLS, "zip = '" + zip + "'",
                        null, null, null, null, null)
      if (c.getCount > 0) {
        c.moveToFirst();
        location = new Location()
        location.id = c.getLong(0)
        location.zip = c.getString(1)
        location.city = c.getString(2)
        location.region = c.getString(3)
        location.lastalert = c.getLong(4)
        location.alertenabled = c.getInt(5)
      }
    } catch {
      case e: SQLException =>
        Log.v(Constants.LOGTAG, CLASSNAME, e)
    } finally {
      if (c != null && !c.isClosed) {
        c.close()
      }
    }
    location
  }

  def getAll: List[Location] = {
    val ret = new ListBuffer[Location]()
    var c: Cursor = null
    try {
      c = this.db.query(DB_TABLE, COLS, null, null, null, null, null)
      val numRows = c.getCount
      c.moveToFirst()
      for (i <- 0 until numRows) {
        val location = new Location()
        location.id = c getLong 0
        location.zip = c getString 1
        location.city = c getString 2
        location.region = c getString 3
        location.lastalert = c getLong 4
        location.alertenabled = c getInt 5
        // don't return special device alert enabled marker location in all list
        if (location.zip != DEVICE_ALERT_ENABLED_ZIP) {
          ret += location
        }
        c.moveToNext()
      }
    } catch {
      case e: SQLException =>
        Log.v(Constants.LOGTAG, CLASSNAME, e)
    } finally {
      if (c != null && !c.isClosed) {
        c.close()
      }
    }
    ret.toList
  }

  def getAllAlertEnabled: List[Location] = {
    var c: Cursor = null
    val ret = new ListBuffer[Location]()
    try {
      c = this.db.query(DB_TABLE, DBHelper.COLS, "alertenabled = 1", null, null, null, null)
      val numRows = c.getCount
      c.moveToFirst()
      for (i <- 0 until numRows) {
        val location = new Location()
        location.id = c getLong 0
        location.zip = c getString 1
        location.city = c getString 2
        location.region = c getString 3
        location.lastalert = c getLong 4
        location.alertenabled = c getInt 5
        // don't return special device alert enabled marker location in all list
        if (location.zip != DBHelper.DEVICE_ALERT_ENABLED_ZIP) {
          ret += location
        }
        c.moveToNext()
      }
    } catch {
      case e: SQLException =>
        Log.v(Constants.LOGTAG, CLASSNAME, e)
    } finally {
      if (c != null && !c.isClosed) {
        c.close()
      }
    }
    ret.toList
  }
}

object DBHelper {

  final val DEVICE_ALERT_ENABLED_ZIP = "DAEZ99"
  final val DB_NAME = "w_alert"
  final val DB_TABLE = "w_alert_loc"
  final val DB_VERSION = 3

  private final val CLASSNAME = classOf[DBHelper].getSimpleName
  private final val COLS = Array("_id", "zip", "city", "region", "lastalert", "alertenabled")

  class Location {
    var id: Long = _
    var lastalert: Long = _
    var alertenabled: Int = _
    var zip: String = _
        // include city and region because geocode is expensive
    var city: String = _
    var region: String = _

    def this(id: Long, lastalert: Long, alertenabled: Int, zip: String,
             city: String, region: String) {
      this()
      this.id = id
      this.lastalert = lastalert
      this.alertenabled = alertenabled
      this.zip = zip
      this.city = city
      this.region = region
    }

    override def toString: String = zip + " " + city + ", " + region
  }

  private class DBOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override def onCreate(db: SQLiteDatabase) {
      try db execSQL DBOpenHelper.DB_CREATE
      catch { case e: SQLException => Log.e(Constants.LOGTAG, CLASSNAME, e) }
    }

    override def onOpen(db: SQLiteDatabase) { super.onOpen(db) }

    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE)
      onCreate(db)
    }
  }

  private object DBOpenHelper {
    private final val DB_CREATE = "CREATE TABLE " + DB_TABLE +
      " (_id INTEGER PRIMARY KEY, zip TEXT UNIQUE NOT NULL, city TEXT, region TEXT, lastalert INTEGER, alertenabled INTEGER);"
  }

}
