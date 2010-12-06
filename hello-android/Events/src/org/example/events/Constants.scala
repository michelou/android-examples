/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.events


import android.net.Uri
import android.provider.BaseColumns

object Constants {
  final val _ID = BaseColumns._ID

  final val TABLE_NAME = "events"

  final val AUTHORITY = "org.example.events";
  final val CONTENT_URI =
    Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME)

 // Columns in the Events database
 final val TIME = "time"
 final val TITLE = "title"
}
