package com.manning.aip.mymoviesdatabase
package data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log

import model.Category

//
// SQLiteOpenHelper   
//
class OpenHelper(context: Context)
extends SQLiteOpenHelper(context, DataConstants.DATABASE_NAME, null, OpenHelper.DATABASE_VERSION) {

  override def onOpen(db: SQLiteDatabase) {
    super.onOpen(db)
    if (!db.isReadOnly) {
      // versions of SQLite older than 3.6.19 don't support foreign keys
      // and neither do any version compiled with SQLITE_OMIT_FOREIGN_KEY
      // http://www.sqlite.org/foreignkeys.html#fk_enable
      // 
      // make sure foreign key support is turned on if it's there
      // (should be already, just a double-checker)          
      db execSQL "PRAGMA foreign_keys=ON;"

      // then we check to make sure they're on 
      // (if this returns no data they aren't even available, so we shouldn't even TRY to use them)
      val c: Cursor = db.rawQuery("PRAGMA foreign_keys", null)
      if (c.moveToFirst()) {
        val result = c getInt 0
        Log.i(Constants.LOG_TAG, "SQLite foreign key support (1 is on, 0 is off): " + result)
      } else {
        // could use this approach in onCreate, and not rely on foreign keys it not available, etc.
        Log.i(Constants.LOG_TAG, "SQLite foreign key support NOT AVAILABLE")
        // if you had to here you could fall back to triggers
      }
      if (!c.isClosed) c.close()
    }
  }

  override def onCreate(db: SQLiteDatabase) {
    Log.i(Constants.LOG_TAG, "DataHelper.OpenHelper onCreate creating database " + DataConstants.DATABASE_NAME)

    CategoryTable.onCreate(db)
    // populate initial categories (one way, there are several, this works ok for small data set)
    // (this is just as an example, MyMovies really doesn't use/need the "category manager"
    val categoryDao = new CategoryDao(db);
    val categories = context.getResources getStringArray R.array.tmdb_categories
    for (cat <- categories)
      categoryDao save new Category(0, cat)

    MovieTable.onCreate(db)

    MovieCategoryTable.onCreate(db)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    Log.i(Constants.LOG_TAG, "SQLiteOpenHelper onUpgrade - oldVersion:" +
                             oldVersion + " newVersion:" + newVersion)

    MovieCategoryTable.onUpgrade(db, oldVersion, newVersion)

    MovieTable.onUpgrade(db, oldVersion, newVersion)

    CategoryTable.onUpgrade(db, oldVersion, newVersion)
  }
}

object OpenHelper {
   private final val DATABASE_VERSION = 1
}
