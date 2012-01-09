package com.manning.aip.mymoviesdatabase
package data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteStatement}
import android.provider.BaseColumns

import scala.collection.mutable.ListBuffer

import MovieTable.MovieColumns
import model.Movie

class MovieDao(db: SQLiteDatabase) extends Dao[Movie] {

  private val insertStatement: SQLiteStatement =
    db compileStatement MovieDao.INSERT

  override def save(entity: Movie): Long = {
    insertStatement.clearBindings()
    insertStatement.bindString(1, entity.getHomepage)
    insertStatement.bindString(2, entity.getName)
    insertStatement.bindDouble(3, entity.getRating)
    insertStatement.bindString(4, entity.getTagline)
    insertStatement.bindString(5, entity.getThumbUrl)
    insertStatement.bindString(6, entity.getImageUrl)
    insertStatement.bindString(7, entity.getTrailer)
    insertStatement.bindString(8, entity.getUrl)
    insertStatement.bindLong(9, entity.getYear)
    insertStatement.executeInsert()
  }

  override def update(entity: Movie) {
    val values = new ContentValues()
    values.put(MovieColumns.HOMEPAGE, entity.getHomepage)
    values.put(MovieColumns.NAME, entity.getName)
    values.put(MovieColumns.RATING, entity.getRating)
    values.put(MovieColumns.TAGLINE, entity.getTagline)
    values.put(MovieColumns.THUMB_URL, entity.getThumbUrl)
    values.put(MovieColumns.IMAGE_URL, entity.getImageUrl)
    values.put(MovieColumns.TRAILER, entity.getTrailer)
    values.put(MovieColumns.URL, entity.getUrl)
    values.put(MovieColumns.YEAR, entity.getYear: java.lang.Integer)
    db.update(MovieTable.TABLE_NAME, values,
              BaseColumns._ID + " = ?", Array(entity.getId.toString))
  }

  override def delete(entity: Movie) {
    if (entity.getId > 0) {
      db.delete(MovieTable.TABLE_NAME, BaseColumns._ID + " = ?", Array(entity.getId.toString))
    }
  }

  // get field slot error, show how it works by removing a column in the columns array here)
  override def get(id: Long): Movie = {
    val c: Cursor = db.query(
      MovieTable.TABLE_NAME,
      Array(BaseColumns._ID, MovieColumns.HOMEPAGE,
            MovieColumns.NAME, MovieColumns.RATING, MovieColumns.TAGLINE, MovieColumns.THUMB_URL,
            MovieColumns.IMAGE_URL, MovieColumns.TRAILER, MovieColumns.URL, MovieColumns.YEAR),
      BaseColumns._ID + " = ?", Array(id.toString), null, null, null, "1")
    val movie =
      if (c.moveToFirst()) this.buildMovieFromCursor(c)
      else null
    if (!c.isClosed) c.close()
    movie
  }

  override def getAll: List[Movie] = {
    val c: Cursor = db.query(
      MovieTable.TABLE_NAME,
      Array(BaseColumns._ID, MovieColumns.HOMEPAGE,
            MovieColumns.NAME, MovieColumns.RATING, MovieColumns.TAGLINE, MovieColumns.THUMB_URL,
            MovieColumns.IMAGE_URL, MovieColumns.TRAILER, MovieColumns.URL, MovieColumns.YEAR),
      null, null, null, null, MovieColumns.NAME, null)
    val list =
      if (c.moveToFirst()) {
        val buf = new ListBuffer[Movie]
        do {
          val movie = this.buildMovieFromCursor(c)
          if (movie != null) buf += movie
        } while (c.moveToNext())
        buf.toList
      }
      else
        null
    if (!c.isClosed) c.close()
    list
  }

  // as an oversimplification our db requires movie names to be unique
  // in real-life, we'd need to return multiple results here (if found)
  // and allow the user to select, or make query use other attributes in combination with name
  // (also note here we expand on the DAO interface definition for just this class)
  def find(name: String): Movie = {
    val sql =
      "select _id from " + MovieTable.TABLE_NAME +
      " where upper(" + MovieColumns.NAME + ") = ? limit 1"
    val c: Cursor = db.rawQuery(sql, Array(name.toUpperCase))
    val movieId = if (c.moveToFirst()) c getLong 0 else 0L
    if (!c.isClosed) c.close()
    // we make another query here, which is another trip, 
    // this is a trade off we accept with such a small amount of data
    this.get(movieId)
  }

  private def buildMovieFromCursor(c: Cursor): Movie = {
    if (c == null) return null
    val movie = new Movie()
    movie setId (c getLong 0)
    movie setHomepage (c getString 1)
    movie setName (c getString 2)
    movie setRating (c getInt 3)
    movie setTagline (c getString 4)
    movie setThumbUrl (c getString 5)
    movie setImageUrl (c getString 6)
    movie setTrailer (c getString 7)
    movie setUrl (c getString 8)
    movie setYear (c getInt 9)
    movie
  }
}

object MovieDao {
  private final val INSERT = "insert into " +
    MovieTable.TABLE_NAME + "(" + MovieColumns.HOMEPAGE + ", " +
    MovieColumns.NAME + ", " + MovieColumns.RATING + ", " +
    MovieColumns.TAGLINE + ", " + MovieColumns.THUMB_URL + ", " +
    MovieColumns.IMAGE_URL + ", " + MovieColumns.TRAILER + ", " +
    MovieColumns.URL + ", " + MovieColumns.YEAR +
    ") values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
}
