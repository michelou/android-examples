package com.manning.aip.mymoviesdatabase
package data

import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteStatement}
import android.provider.BaseColumns

import scala.collection.mutable.ListBuffer

import CategoryTable.CategoryColumns
import MovieCategoryTable.MovieCategoryColumns
import model.Category

// special DAO in this case, so doesn't implement Dao
// doesn't return an entity, and key is not long (composite key)
// this is used for the movie/category mapping table (not for an entity)
class MovieCategoryDao(db: SQLiteDatabase) {

  private val insertStatement: SQLiteStatement =
    db compileStatement MovieCategoryDao.INSERT

  def save(entity: MovieCategoryKey): Long = {
    insertStatement.clearBindings()
    insertStatement.bindLong(1, entity.movieId)
    insertStatement.bindLong(2, entity.categoryId)
    insertStatement.executeInsert()
  }
   
  def delete(key: MovieCategoryKey) {
    if ((key.movieId > 0) && (key.categoryId > 0)) {
      db.delete(MovieCategoryTable.TABLE_NAME,
                MovieCategoryColumns.MOVIE_ID + " = ? and " +
                MovieCategoryColumns.CATEGORY_ID + " = ?",
                Array(key.movieId.toString, key.categoryId.toString))
    }
  }

  def exists(key: MovieCategoryKey): Boolean = {
    val c: Cursor = db.query(
      MovieCategoryTable.TABLE_NAME,
      Array(MovieCategoryColumns.MOVIE_ID, MovieCategoryColumns.CATEGORY_ID),
      MovieCategoryColumns.MOVIE_ID + " = ? and " +
      MovieCategoryColumns.CATEGORY_ID + " = ?",
      Array(key.movieId.toString, key.categoryId.toString),
      null, null, null, "1")
    val result = c.moveToFirst()
    if (!c.isClosed) c.close()
    result
  }

  def getCategories(movieId: Long): List[Category] = {
    // join movie_category and category, so we can get category name in one query
    val sql = "select " +
      MovieCategoryColumns.CATEGORY_ID + ", " + CategoryColumns.NAME + " from " +
      MovieCategoryTable.TABLE_NAME + ", " + CategoryTable.TABLE_NAME + " where " +
      MovieCategoryColumns.MOVIE_ID + " = ? and " + MovieCategoryColumns.CATEGORY_ID + " = " +
      BaseColumns._ID
    val c: Cursor = db.rawQuery(sql, Array(movieId.toString))
    val list =
      if (c.moveToFirst()) {
        val buf = new ListBuffer[Category]
        do buf += new Category(c getLong 0, c getString 1)
        while (c.moveToNext())
        buf.toList
      }
      else
        null
    if (!c.isClosed) c.close()
    list
  }

  // if we had more sophisticated searching we might want movies by category
  /*
  def getMovies(categoryId: Long): List[Movie] = {
    val buf = new ListBuffer[Movie]
      
    buf.toList
  }
  */
}

object MovieCategoryDao {
  private final val INSERT = "insert into " +
    MovieCategoryTable.TABLE_NAME + "(" +
    MovieCategoryColumns.MOVIE_ID + ", " +
    MovieCategoryColumns.CATEGORY_ID + ") values (?, ?)"
}
