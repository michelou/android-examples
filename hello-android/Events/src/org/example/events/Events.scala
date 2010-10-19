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
import android.app.ListActivity
import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.widget.SimpleCursorAdapter

class Events extends ListActivity {
  import Events._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    addEvent("Hello, Android!")
    val cursor = getEvents
    showEvents(cursor)
  }

  private def addEvent(title: String) {
    // Insert a new record into the Events data source.
    // You would do something similar for delete and update.
    val values = new ContentValues()
    values.put(TIME, java.lang.Float.valueOf(System.currentTimeMillis))
    values.put(TITLE, title)
    getContentResolver.insert(CONTENT_URI, values)
  }

  private def getEvents: Cursor = {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    managedQuery(CONTENT_URI, FROM, null, null, ORDER_BY)
  }

  private def showEvents(cursor: Cursor) {
    // Set up data binding
    val adapter = new SimpleCursorAdapter(this, R.layout.item, cursor, FROM, TO)
    setListAdapter(adapter)
  }
}

object Events {
  private val FROM     = Array(_ID, TIME, TITLE)
  private val TO       = Array(R.id.rowid, R.id.time, R.id.title)
  private val ORDER_BY = TIME + " DESC"
}
