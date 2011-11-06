package com.apress.contentreceiverexample;

import android.app.Activity
import android.content.{ContentProvider, ContentResolver, ContentValues}
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.util.Log
import android.view.KeyEvent

class MainActivity extends Activity {
  import MainActivity._  // companion object

  /** Called when the activity is first created. */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)
  }

  private def doesBookmarkExist0(url: String): Boolean = {
    try {
      val projection = Array(
        android.provider.BaseColumns._ID,
        Browser.BookmarkColumns.URL,
        Browser.BookmarkColumns.TITLE
      )
      //Cursor managedQuery(Uri uri, String[] projection,
      //String selection, String[] selectionArgs, String sortOrder)
      val results = managedQuery(Browser.BOOKMARKS_URI, projection, 
        null, null, Browser.BookmarkColumns.URL + " ASC")
      val urlColumn = results getColumnIndex Browser.BookmarkColumns.URL

      var hasMore = results.moveToFirst()
      while (hasMore) {
        if (results.getString(urlColumn) equals url)
          return true
        hasMore = results.move(1)
      }
      false
    } catch {
      case e: Exception =>
        System.out.print("Suck it Trabeck")
        false
    }
  }

  private def doesBookmarkExist(url: String): Boolean = {
    val bookmarks = Browser.getAllBookmarks(getContentResolver)
    val urlColumn = bookmarks getColumnIndex Browser.BookmarkColumns.URL

    var hasMore = bookmarks.moveToFirst()
    while (hasMore) {
      if (bookmarks.getString(urlColumn) equals url)
        return true
        hasMore = bookmarks.move(1)
    }
    false
  }
    
  private def addBookmark(url: String, title: String) {
    val inputValues = new ContentValues()
    inputValues.put(Browser.BookmarkColumns.BOOKMARK, "1")
    inputValues.put(Browser.BookmarkColumns.URL, url)
    inputValues.put(Browser.BookmarkColumns.TITLE, title)
    inputValues.put(Browser.BookmarkColumns.CREATED, "5/5/01")

    val cr = getContentResolver
    var uri = cr.insert(Browser.BOOKMARKS_URI, inputValues)
    //Browser.saveBookmark(this, title, url);
  }

  override def onKeyDown(keycode: Int, k: KeyEvent): Boolean = {
    //if (!doesBookmarkExist0(URL_APRESS)) {
    if (!doesBookmarkExist(URL_APRESS)) {
      Log.i(TAG, "add "+URL_APRESS);
      addBookmark(URL_APRESS, "Apress")
    }
    true
  }
}

object MainActivity {
  private val TAG = "MainActivity"

  private val URL_APRESS = "http://www.apress.com/"
}
