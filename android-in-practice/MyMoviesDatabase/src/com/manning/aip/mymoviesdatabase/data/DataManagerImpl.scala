package com.manning.aip.mymoviesdatabase
package data

import android.content.Context
import android.database.{Cursor, SQLException}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.os.SystemClock
import android.util.Log

import java.util.{List => JList}

import MovieTable.MovieColumns;
import model.{Category, Movie}

/**
 * Android DataManagerImpl to encapsulate SQL and DB details.
 * Includes SQLiteOpenHelper, and uses Dao objects
 * to create/update/delete and otherwise manipulate data.
 *
 * @author ccollins
 *
 */
class DataManagerImpl(context: Context) extends DataManager {
  private var db: SQLiteDatabase = {
    val openHelper = new OpenHelper(context)
    val db = openHelper.getWritableDatabase
    Log.i(Constants.LOG_TAG, "DataManagerImpl created, db open status: " + db.isOpen)
    db
  }
  private var categoryDao = new CategoryDao(db)
  private var movieDao = new MovieDao(db)
  private var movieCategoryDao = new MovieCategoryDao(db)

  def getDb: SQLiteDatabase = db

  private def openDb() {
    if (!db.isOpen) {
      db = SQLiteDatabase.openDatabase(DataConstants.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE)
      // since we pass db into DAO, have to recreate DAO if db is re-opened
      categoryDao = new CategoryDao(db)
      movieDao = new MovieDao(db)
      movieCategoryDao = new MovieCategoryDao(db)
    }
  }

  private def closeDb() {
    if (db.isOpen) db.close()
  }

  private def resetDb() {
    Log.i(Constants.LOG_TAG, "Resetting database connection (close and re-open).")
    closeDb()
    SystemClock.sleep(500)
    openDb()
  }

  //
  // "Manager" data methods that wrap DAOs
  //
  // this lets us encapsulate usage of DAOs 
  // we only expose methods app is actually using, and we can combine DAOs, with logic in one place
  //  

  // movie
  override def getMovie(movieId: Long): Movie = {
    val movie = movieDao get movieId
    if (movie != null) {
      movie.getCategories ++= (movieCategoryDao getCategories movie.getId)
    }
    movie
  }

  override def getMovieHeaders: List[Movie] = {
    // these movies don't have categories, but they're really used as "headers" anyway, it's ok
    movieDao.getAll
  }

  override def findMovie(name: String): Movie = {
    val movie = movieDao find name
    if (movie != null) {
      movie.getCategories ++= (movieCategoryDao getCategories movie.getId)
    }
    movie
  }

  override def saveMovie(movie: Movie): Long = {
    // NOTE could wrap entity manip functions in DataManagerImpl, make "manager"
    // for each entity here though, to keep it simpler, we use the DAOs directly
    // (even when multiple are involved)
    var movieId = 0L

    // put it in a transaction, since we're touching multiple tables
    try {
      db.beginTransaction()

      // first save movie                                 
      movieId = movieDao save movie

      // second, make sure categories exist, and save movie/category association
      // (this makes multiple queries, but usually not many cats, could just
      // save and catch exception too, but that's ugly)
      for (c <- movie.getCategories) {
        val dbCat = categoryDao find c.getName
        val catId =
          if (dbCat == null) categoryDao save c
          else dbCat.getId
        val mcKey = new MovieCategoryKey(movieId, catId)
        if (!movieCategoryDao.exists(mcKey))
          movieCategoryDao save mcKey
      }

      db.setTransactionSuccessful()
    } catch {
      case e: SQLException =>
        Log.e(Constants.LOG_TAG, "Error saving movie (transaction rolled back)", e)
        movieId = 0L
    } finally {
      // an "alias" for commit
      db.endTransaction()
    }

    movieId
  }

  override def deleteMovie(movieId: Long): Boolean = {
    var result = false
    // NOTE switch this order around to see constraint error (foreign keys work)
    try {
      db.beginTransaction()
      // make sure to use getMovie and not movieDao directly, categories need to be included
      val movie = getMovie(movieId)
      if (movie != null) {
        for (c <- movie.getCategories) {
          movieCategoryDao delete MovieCategoryKey(movie.getId, c.getId)
        }
        movieDao delete movie
      }
      db.setTransactionSuccessful()
      result = true
    } catch {
      case e: SQLException =>
         Log.e(Constants.LOG_TAG, "Error deleting movie (transaction rolled back)", e)
    } finally {
      db.endTransaction()
    }
    result
  }

  override def getMovieCursor: Cursor = {
    // note that query MUST have a column named _id
    db.rawQuery("select " + MovieColumns._ID + ", " +
                MovieColumns.NAME + ", " + MovieColumns.THUMB_URL +
                " from " + MovieTable.TABLE_NAME, null)
  }

  // category
  override def getCategory(categoryId: Long): Category =
    categoryDao get categoryId

  override def getAllCategories: List[Category] =
    categoryDao.getAll

  override def findCategory(name: String): Category =
    categoryDao find name

  override def saveCategory(category: Category): Long =
    categoryDao save category

  override def deleteCategory(category: Category) {
    categoryDao delete category
  }

}
