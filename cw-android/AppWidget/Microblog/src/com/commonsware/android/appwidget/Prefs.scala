/* Copyright (c) 2008-10 -- CommonsWare, LLC

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
   
package com.commonsware.android.appwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.{ComponentName, Intent}
import android.os.{Build, Bundle}
import android.preference.PreferenceActivity
import android.view.KeyEvent
import android.widget.RemoteViews

class Prefs extends PreferenceActivity {
  import Prefs._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    addPreferencesFromResource(R.xml.preferences)
  }

  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    if (keyCode == KeyEvent.KEYCODE_BACK && Build.VERSION.SDK.toInt < 5) {
      onBackPressed()
    }

    super.onKeyDown(keyCode, event)
  }

  override def onBackPressed() {
    if (CONFIGURE_ACTION equals getIntent.getAction) {
      val intent = getIntent
      val extras = intent.getExtras

      if (extras != null) {
        val id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                               AppWidgetManager.INVALID_APPWIDGET_ID)
        val mgr = AppWidgetManager.getInstance(this)
        val views = new RemoteViews(getPackageName, R.layout.widget)

        mgr.updateAppWidget(id, views)

        val result = new Intent()
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        setResult(Activity.RESULT_OK, result)

        val update = new Intent(this, classOf[AppWidget])
        update.setAction(AppWidget.REFRESH)
        sendBroadcast(update)
      }
    }

    super.onBackPressed()
  } 
}

object Prefs {

  private val CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE"

}
