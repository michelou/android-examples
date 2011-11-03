/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.supportv4.app

import android.support.v4.app.{FragmentActivity, FragmentManager, ListFragment, LoaderManager}
import android.support.v4.content.{CursorLoader, Loader}
import android.support.v4.widget.SimpleCursorAdapter

import android.content.{ContentProvider, ContentResolver, ContentUris,
                        ContentValues, Context, UriMatcher}
import android.database.{Cursor, DatabaseUtils, SQLException}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.os.{AsyncTask, Bundle}
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import android.view.{Menu, MenuInflater, MenuItem, View}
import android.widget.ListView

import java.lang.{Void => JVoid}
import scala.collection.JavaConversions._

/**
 * Demonstration of bottom to top implementation of a content provider holding
 * structured data through displaying it in the UI, using throttling to reduce
 * the number of queries done when its data changes.
 */
class LoaderThrottleSupport extends FragmentActivity {
  import LoaderThrottleSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val fm = getSupportFragmentManager

    // Create the list fragment and add it as our sole content.
    if (fm.findFragmentById(android.R.id.content) == null) {
      val list = new ThrottledLoaderListFragment()
      fm.beginTransaction().add(android.R.id.content, list).commit()
    }
  }

}

object LoaderThrottleSupport {

  // Debugging.
  private final val TAG = "LoaderThrottle"

  /**
   * The authority we use to get to our sample provider.
   */
  private final val AUTHORITY =
    "com.example.android.apis.supportv4.app.LoaderThrottle"

  /**
   * Definition of the contract for the main table of our provider.
   */
  object MainTable {

    final val _ID = android.provider.BaseColumns._ID

    /**
     * The table name offered by this provider
     */
    final val TABLE_NAME = "main"

    /**
     * The content:// style URL for this table
     */
    final val CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/main")

    /**
     * The content URI base for a single row of data. Callers must
     * append a numeric row id to this Uri to retrieve a row
     */
    final val CONTENT_ID_URI_BASE =
      Uri.parse("content://" + AUTHORITY + "/main/")

    /**
     * The MIME type of {@link #CONTENT_URI}.
     */
    final val CONTENT_TYPE =
      "vnd.android.cursor.dir/vnd.example.api-demos-throttle"

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
     */
    final val CONTENT_ITEM_TYPE =
       "vnd.android.cursor.item/vnd.example.api-demos-throttle"

    /**
     * The default sort order for this table
     */
    final val DEFAULT_SORT_ORDER = "data COLLATE LOCALIZED ASC"

    /**
     * Column name for the single column holding our data.
     * <P>Type: TEXT</P>
     */
    final val COLUMN_NAME_DATA = "data"
  }

  private object DatabaseHelper {
    private final val DATABASE_NAME = "loader_throttle.db"
    private final val DATABASE_VERSION = 2
  }
  import DatabaseHelper._

  /**
   * This class helps open, create, and upgrade the database file.
   */
  private class DatabaseHelper(context: Context)
  extends SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     *
     * Creates the underlying database with table name and column names taken from the
     * NotePad class.
     */
    override def onCreate(db: SQLiteDatabase) {
      db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " ("
                   + MainTable._ID + " INTEGER PRIMARY KEY,"
                   + MainTable.COLUMN_NAME_DATA + " TEXT"
                   + ");")
    }

    /**
     *
     * Demonstrates that the provider must consider what happens when the
     * underlying datastore is changed. In this sample, the database is upgraded the database
     * by destroying the existing data.
     * A real application should upgrade the database in place.
     */
    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      // Logs that the database is being upgraded
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                   + newVersion + ", which will destroy all old data")

      // Kills the table and existing data
      db.execSQL("DROP TABLE IF EXISTS notes")

      // Recreates the database with a new version
      onCreate(db)
    }
  }

  private object SimpleProvider {

    // The incoming URI matches the main table URI pattern
    private final val MAIN = 1

    // The incoming URI matches the main table row ID URI pattern
    private final val MAIN_ID = 2

    // java.util.Arrays.copyOf(selArgs, n+1); // Java 1.6
    private def copyOf(original: Array[String], newLength: Int): Array[String] = {
      val result = new Array[String](newLength)
      System.arraycopy(original, 0, result, 0, newLength)
      result
    }

    // A projection map used to select columns from the database
    //  This is simply an identity mapping.
    private val NOTES_PROJECTION = Map(
      MainTable._ID -> MainTable._ID,
      MainTable.COLUMN_NAME_DATA -> MainTable.COLUMN_NAME_DATA
    )

    // Uri matcher to decode incoming URIs.
    private val URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH)
    URI_MATCHER.addURI(AUTHORITY, MainTable.TABLE_NAME, MAIN)
    URI_MATCHER.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", MAIN_ID)

    private def getId(uri: Uri): Int = URI_MATCHER `match` uri
  }

  /**
   * A very simple implementation of a content provider.
   */
  private class SimpleProvider extends ContentProvider {
    import SimpleProvider._

    // Handle to a new DatabaseHelper.
    private var mOpenHelper: DatabaseHelper = _

    /**
     * Perform provider creation.
     */
    override def onCreate(): Boolean = {
      mOpenHelper = new DatabaseHelper(getContext)
      // Assumes that any failures will be reported by a thrown exception.
      true
    }

    /**
     * Handle incoming queries.
     */
    override def query(uri: Uri, projection: Array[String], selection: String,
                       selectionArgs: Array[String], sortOrder: String): Cursor = {

      // Constructs a new query builder and sets its table name
      val qb = new SQLiteQueryBuilder()
      qb setTables MainTable.TABLE_NAME

      var selArgs = selectionArgs
      getId(uri) match {
        case MAIN =>
          // If the incoming URI is for main table.
          qb setProjectionMap NOTES_PROJECTION

        case MAIN_ID =>
          // The incoming URI is for a single row.
          qb setProjectionMap NOTES_PROJECTION
          qb.appendWhere(MainTable._ID + "=?")
          // BEGIN:API 11
          //selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
          //        new String[] { uri.getLastPathSegment() });
          // END:API 11
          val n = selArgs.length
          selArgs = copyOf(selArgs, n+1)
          selArgs(n) = uri.getLastPathSegment

        case _ =>
          throw new IllegalArgumentException("Unknown URI " + uri)
      }

      val _sortOrder =
        if (TextUtils isEmpty sortOrder) MainTable.DEFAULT_SORT_ORDER
        else sortOrder

      val db = mOpenHelper.getReadableDatabase
      val c = qb.query(db, projection, selection, selArgs,
                       null /* no group */, null /* no filter */, _sortOrder)

      c.setNotificationUri(getContext.getContentResolver, uri)
      c
    }

    /**
     * Return the MIME type for an known URI in the provider.
     */
    override def getType(uri: Uri): String =
      getId(uri) match {
        case MAIN    => MainTable.CONTENT_TYPE
        case MAIN_ID => MainTable.CONTENT_ITEM_TYPE
        case _       => throw new IllegalArgumentException("Unknown URI " + uri)
      }

    /**
     * Handler inserting new data.
     */
    override def insert(uri: Uri, initialValues: ContentValues): Uri = {
      if (getId(uri) != MAIN) {
        // Can only insert into to main URI.
        throw new IllegalArgumentException("Unknown URI " + uri)
      }

      val values =
        if (initialValues != null) new ContentValues(initialValues)
        else new ContentValues()

      if (!values.containsKey(MainTable.COLUMN_NAME_DATA)) {
        values.put(MainTable.COLUMN_NAME_DATA, "")
      }

      val db = mOpenHelper.getWritableDatabase
      val rowId = db.insert(MainTable.TABLE_NAME, null, values)

      // If the insert succeeded, the row ID exists.
      if (rowId > 0) {
        val noteUri = ContentUris.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId)
        getContext.getContentResolver.notifyChange(noteUri, null)
        noteUri
      }
      else
        throw new SQLException("Failed to insert row into " + uri)
    }

    /**
     * Handle deleting data.
     */
    override def delete(uri: Uri, where: String, whereArgs: Array[String]): Int = {
      val db = mOpenHelper.getWritableDatabase
      val count = getId(uri) match {
        case MAIN =>
          // If URI is main table, delete uses incoming where clause and args.
          db.delete(MainTable.TABLE_NAME, where, whereArgs)

        // If the incoming URI matches a single note ID, does the delete based on the
        // incoming data, but modifies the where clause to restrict it to the
        // particular note ID.
        case MAIN_ID =>
          // If URI is for a particular row ID, delete is based on incoming
          // data but modified to restrict to the given ID.
          // BEGIN:API 11
          //finalWhere = DatabaseUtils.concatenateWhere(
          //        MainTable._ID + " = " + ContentUris.parseId(uri), where);
          // END:API 11
          val finalWhere = where + " AND " + MainTable._ID + " = " + ContentUris.parseId(uri)
          db.delete(MainTable.TABLE_NAME, finalWhere, whereArgs)

        case _ =>
          throw new IllegalArgumentException("Unknown URI " + uri)
      }

      getContext.getContentResolver.notifyChange(uri, null)

      count
    }

    /**
     * Handle updating data.
     */
    override def update(uri: Uri, values: ContentValues, where: String,
                        whereArgs: Array[String]): Int = {
      val db = mOpenHelper.getWritableDatabase
      val count = getId(uri) match {
        case MAIN =>
          // If URI is main table, update uses incoming where clause and args.
          db.update(MainTable.TABLE_NAME, values, where, whereArgs)

        case MAIN_ID =>
          // If URI is for a particular row ID, update is based on incoming
          // data but modified to restrict to the given ID.
          // BEGIN:API 11
          //finalWhere = DatabaseUtils.concatenateWhere(
          //        MainTable._ID + " = " + ContentUris.parseId(uri), where);
          // END:API 11
          val finalWhere = where + " AND " + MainTable._ID + " = " + ContentUris.parseId(uri)
          db.update(MainTable.TABLE_NAME, values, finalWhere, whereArgs)

        case _ =>
          throw new IllegalArgumentException("Unknown URI " + uri)
      }

      getContext.getContentResolver.notifyChange(uri, null)

      count
    }
  }

  private object ThrottledLoaderListFragment {

    // Menu identifiers
    final val POPULATE_ID = Menu.FIRST
    final val CLEAR_ID = Menu.FIRST+1

    // These are the rows that we will retrieve.
    final val PROJECTION = Array(MainTable._ID, MainTable.COLUMN_NAME_DATA)
  }

  private[app] class ThrottledLoaderListFragment extends ListFragment
            with LoaderManager.LoaderCallbacks[Cursor] {
    import ThrottledLoaderListFragment._  // companion object

    // This is the Adapter being used to display the list's data.
    private var mCursorAdapter: SimpleCursorAdapter = _

    // If non-null, this is the current filter the user has provided.
    private var mCurFilter: String = _

    // Task we have running to populate the database.
    //private var mPopulatingTask: AsyncTask[AnyRef, AnyRef, AnyRef] = _
    private var mPopulatingTask: MyAsyncTask[AnyRef, AnyRef, AnyRef] = _

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      setEmptyText("No data.  Select 'Populate' to fill with data from Z to A at a rate of 4 per second.")
      setHasOptionsMenu(true)

      // Create an empty adapter we will use to display the loaded data.
      mCursorAdapter = new SimpleCursorAdapter(getActivity,
                    android.R.layout.simple_list_item_1, null,
                    Array(MainTable.COLUMN_NAME_DATA),
                    Array(android.R.id.text1), 0)
      setListAdapter(mCursorAdapter)

      // Start out with a progress indicator.
      setListShown(false)

      // Prepare the loader.  Either re-connect with an existing one,
      // or start a new one.
      getLoaderManager.initLoader(0, null, this)
    }

    override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      menu.add(Menu.NONE, POPULATE_ID, 0, "Populate")
                    //.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
      menu.add(Menu.NONE, CLEAR_ID, 0, "Clear")
                    //.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    override def onOptionsItemSelected(item: MenuItem): Boolean = {
      val cr = getActivity.getContentResolver

      item.getItemId match {
        case POPULATE_ID =>
          if (mPopulatingTask != null) {
            mPopulatingTask cancel false
          }
          //mPopulatingTask = new AsyncTask[AnyRef, AnyRef, AnyRef]() {
          //  override protected def doInBackground(params: AnyRef*): AnyRef = {
          mPopulatingTask = new MyAsyncTask[AnyRef, AnyRef, AnyRef]() {
            protected def doInBackground1(params: Array[AnyRef with Object]): AnyRef = {
              for (c <- 'Z'.toInt to 'A'.toInt by -1 if !isCancelled) {
                val builder = new StringBuilder("Data ")
                builder append c.toChar
                val values = new ContentValues()
                values.put(MainTable.COLUMN_NAME_DATA, builder.toString)
                cr.insert(MainTable.CONTENT_URI, values)
                // Wait a bit between each insert.
                try Thread.sleep(250)
                catch { case e: InterruptedException => }
              }
              null
            }
          }
          // BEGIN:API 11
          //mPopulatingTask.executeOnExecutor(
          //        AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
          // END:API 11
          mPopulatingTask execute null.asInstanceOf[AnyRef]
          true

        case CLEAR_ID =>
          if (mPopulatingTask != null) {
            mPopulatingTask cancel false
            mPopulatingTask = null
          }
          val task = new AsyncTask[AnyRef, AnyRef, AnyRef]() {
            override protected def doInBackground(params: AnyRef*): AnyRef = {
              cr.delete(MainTable.CONTENT_URI, null, null)
              null
            }
          }
          task execute null.asInstanceOf[Array[AnyRef]]
          true

        case _ =>
          super.onOptionsItemSelected(item)
      }
    }

    override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
      // Insert desired behavior here.
      Log.i(TAG, "Item clicked: " + id)
    }

    def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
      val cl = new CursorLoader(getActivity(), MainTable.CONTENT_URI,
                    PROJECTION, null, null, null)
      cl setUpdateThrottle 2000  // update at most every 2 seconds.
      cl
    }

    def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
      mCursorAdapter swapCursor data

      // The list should now be shown.
      if (isResumed) setListShown(true)
      else setListShownNoAnimation(true)
    }

    def onLoaderReset(loader: Loader[Cursor]) {
      mCursorAdapter swapCursor null
    }
  }

}


