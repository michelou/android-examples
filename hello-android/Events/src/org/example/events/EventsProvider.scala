/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.events

import android.provider.BaseColumns._ID
import org.example.events.Constants._
import android.content.{ContentProvider, ContentUris, ContentValues, UriMatcher}
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.text.TextUtils

class EventsProvider extends ContentProvider {
  import EventsProvider._  // companion object

  private var events: EventsData = _
  private var uriMatcher: UriMatcher = _

  override def onCreate(): Boolean = {
    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH)
    uriMatcher.addURI(AUTHORITY, "events", EVENTS)
    uriMatcher.addURI(AUTHORITY, "events/#", EVENTS_ID)
    events = new EventsData(getContext)
    true
  }

  override def query(uri: Uri, projection: Array[String],
                     selection0: String, selectionArgs: Array[String],
                     orderBy: String): Cursor = {
    val selection = if (uriMatcher.`match`(uri) == EVENTS_ID) {
      val id = /*Long.parseLong(*/uri.getPathSegments.get(1).toLong
      appendRowId(selection0, id)
    } else
      selection0
    // Get the database and run the query
    val db = events.getReadableDatabase()
    val cursor = db.query(TABLE_NAME, projection, selection,
            selectionArgs, null, null, orderBy)

    // Tell the cursor what uri to watch, so it knows when its
    // source data changes
    cursor.setNotificationUri(getContext.getContentResolver, uri)
    cursor
  }

  override def getType(uri: Uri): String = {
    uriMatcher `match` uri match {
      case EVENTS =>
        CONTENT_TYPE
      case EVENTS_ID =>
        CONTENT_ITEM_TYPE
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    val db = events.getWritableDatabase()

    // Validate the requested uri
    if (uriMatcher.`match`(uri) != EVENTS) {
      throw new IllegalArgumentException("Unknown URI " + uri)
    }

    // Insert into database
    val id = db.insertOrThrow(TABLE_NAME, null, values)

    // Notify any watchers of the change
    val newUri = ContentUris.withAppendedId(CONTENT_URI, id)
    getContext.getContentResolver.notifyChange(newUri, null)
    newUri
  }

  override def delete(uri: Uri, selection: String,
                      selectionArgs: Array[String]): Int = {
    val db = events.getWritableDatabase()
    val count = uriMatcher `match` uri match {
      case EVENTS =>
        db.delete(TABLE_NAME, selection, selectionArgs)
      case EVENTS_ID =>
        val id = uri.getPathSegments.get(1).toLong
        db.delete(TABLE_NAME, appendRowId(selection, id), selectionArgs)
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
    }

    // Notify any watchers of the change
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def update(uri: Uri, values: ContentValues,
                      selection: String, selectionArgs: Array[String]): Int = {
    val db = events.getWritableDatabase()
    val count = uriMatcher `match` uri match {
      case EVENTS =>
        db.update(TABLE_NAME, values, selection, selectionArgs)
      case EVENTS_ID =>
        val id = uri.getPathSegments().get(1).toLong
        db.update(TABLE_NAME, values, appendRowId(selection, id), selectionArgs)
      case _ =>
        throw new IllegalArgumentException("Unknown URI " + uri)
     }

    // Notify any watchers of the change
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  /** Append an id test to a SQL selection expression */
  private def appendRowId(selection: String, id: Long): String =
    _ID + "=" + id +
    (if (!TextUtils.isEmpty(selection)) " AND (" + selection + ')'
     else "")

}
object EventsProvider {
  private final val EVENTS = 1
  private final val EVENTS_ID = 2

  /** The MIME type of a directory of events */
  private final val CONTENT_TYPE =
    "vnd.android.cursor.dir/vnd.example.event"

  /** The MIME type of a single event */
  private final val CONTENT_ITEM_TYPE =
    "vnd.android.cursor.item/vnd.example.event"
}
