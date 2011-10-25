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

import android.app.{IntentService, PendingIntent}
import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.{ComponentName, Context, Intent, SharedPreferences}
import android.preference.PreferenceManager
import android.widget.RemoteViews
import winterwell.jtwitter.Twitter

class AppWidget extends AppWidgetProvider {
  import AppWidget._  // companion object

  override def onReceive(context: Context, intent: Intent) {
    if (REFRESH equals intent.getAction)
      context startService new Intent(context, classOf[UpdateService])
    else
      super.onReceive(context, intent)
  }

  override def onUpdate(context: Context, mgr: AppWidgetManager,
                        appWidgetIds: Array[Int]) {
    context startService new Intent(context, classOf[UpdateService])
  }

}

object AppWidget {

  final val REFRESH = "com.commonsware.android.appwidget.REFRESH"

  class UpdateService extends IntentService("AppWidget$UpdateService") {
    private var prefs: SharedPreferences = _

    override def onCreate() {
      super.onCreate()
      
      prefs = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override def onHandleIntent(intent: Intent) {
      val me = new ComponentName(this, classOf[AppWidget])
      val mgr = AppWidgetManager.getInstance(this)

      mgr.updateAppWidget(me, buildUpdate(this))
    }

    private def buildUpdate(context: Context): RemoteViews = {
      val updateViews = new RemoteViews(context.getPackageName, R.layout.widget)
      val user = prefs.getString("user", null)
      val password = prefs.getString("password", null)
      val service_url = prefs.getString("service_url", "")

      if (user != null && password != null) {
        val client = new Twitter(user, password)
        
        if (service_url != null && service_url.length > 0) {
          client setAPIRootUrl service_url
        }

        var timeline: java.util.List[Twitter.Status] = null

        try {
          for (i <- 0 until 10 if timeline == null) {
            timeline = client.getFriendsTimeline
          }
        }
        catch {
          case e: NullPointerException =>
            // means JTwitter and identi.ca are not getting along
        }

        if (timeline.size > 0) {
          val s: Twitter.Status = timeline.get(0)

          updateViews.setTextViewText(R.id.friend,
                                      s.user.screenName)
          updateViews.setTextViewText(R.id.status,
                                      s.text)

          var i = new Intent(this, classOf[AppWidget])
          i setAction REFRESH
          var pi = PendingIntent.getBroadcast(context, 0, i, 0)
          updateViews.setOnClickPendingIntent(R.id.refresh, pi)

          i = new Intent(this, classOf[Prefs])
          pi = PendingIntent.getActivity(context, 0  , i, 0)
          updateViews.setOnClickPendingIntent(R.id.configure, pi)
        }
      }

      updateViews
    }
  }
}
