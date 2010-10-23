/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.widget

import java.text.SimpleDateFormat
import java.util.Date

import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.Context
import android.widget.RemoteViews

class Widget extends AppWidgetProvider {

  // Define the format string for the date
  private val formatter = new SimpleDateFormat("EEEEEEEEE\nd MMM yyyy")

  override def onUpdate(context: Context,
                        appWidgetManager: AppWidgetManager,
                        appWidgetIds: Array[Int]) {
    // Retrieve and format the current date
    val now = formatter format new Date()

    // Change the text in the widget
    val updateViews = new RemoteViews(context.getPackageName, R.layout.main)
    updateViews.setTextViewText(R.id.text, now)
    appWidgetManager.updateAppWidget(appWidgetIds, updateViews)

    // Not really necessary, just a habit
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

}
