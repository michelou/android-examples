package com.manning.aip.lifecycle

import android.app.{Activity, Notification, NotificationManager, PendingIntent}
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.{RemoteViews, Toast}

// logcat filterspec
// adb -e logcat "*:s LifecycleExplorer:v"

// see also: adb shell dumpsys activity

/**
 * The lifecycle and task affinity of an Activity should be well understood
 * when writing Android apps. This class can help expose what the platform
 * is doing with regard to lifecycle/tasks (it uses Toasts and logging to
 * emphasize each method call). 
 * 
 * Four Activity states:
 * ACTIVE/RUNNING (in the foreground)
 * PAUSED (lost focus, but still visible -- another non-full-sized Activity on top of this one)
 * STOPPED (completely obscured by another Activity)
 * KILLED (paused or stopped may be finished or killed)
 * 
 * Three key loops to keep in mind:
 * ENTIRE LIFETIME - onCreate to onDestroy
 * VISIBLE LIFETIME - onStart to onStop
 * FOREGROUND LIFETIME - onResume to onPause
 * 
 * Three key methods (though there are also many more):
 * onCreate
 * onResume
 * onPause
 * 
 * http://developer.android.com/intl/de/reference/android/app/Activity.html
 * 
 * Note: Use logcat filterspec to see just these debug statements:
 * adb -e logcat "*:s LifecycleExplorer:v"
 * 
 * Note: Construct with false to disable Notifications (which can be slow when there are many).
 * 
 * @author ccollins
 *
 */
abstract class LifecycleActivity(val enableNotifications: Boolean) extends Activity {
  import LifecycleActivity._  // companion object

  private var notifyMgr: NotificationManager = _
   
  private final val className = this.getClass.getName

  // default this to true, or use the ctor, to send notifications
  // with many events notifications can be slow, but useful to "see" what's happening
  def this() = this(true)

  override def onCreate(savedInstanceState: Bundle) {      
    super.onCreate(savedInstanceState)
    notifyMgr = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    debugEvent("onCreate")
  }

  override protected def onStart() {
    debugEvent("onStart")
    super.onStart()
  }

  override protected def onResume() {
    debugEvent("onResume")
    super.onResume();
  }

  override protected def onPause() {
    debugEvent("onPause")
    super.onPause()
  }

  override protected def onStop() {
    debugEvent("onStop")
    super.onStop()
  }

  override protected def onDestroy() {
    debugEvent("onDestroy")
    super.onDestroy()
  }

  //
  // state related
  //
  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    debugEvent("onRestoreInstanceState")
    super.onRestoreInstanceState(savedInstanceState)
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    debugEvent("onSaveInstanceState")
    super.onSaveInstanceState(outState)
  }

  //
  // configuration related 
  //
  override def onConfigurationChanged(newConfig: Configuration) {      
    debugEvent("onConfigurationChanged")
    super.onConfigurationChanged(newConfig)
  }

  override def onRetainNonConfigurationInstance(): AnyRef = {
    debugEvent("onRetainNonConfigurationInstance")
    super.onRetainNonConfigurationInstance()
  }

  //
  // other handy Activity methods
  //
  override def isFinishing: Boolean = {
    debugEvent("isFinishing")
    super.isFinishing
  }

  override def finish() {
    super.finish()
  }

  override def onLowMemory() {
    Toast.makeText(this, "onLowMemory", Toast.LENGTH_SHORT).show()
    super.onLowMemory()
  }

  //
  // notify helper
  //
  private def debugEvent(method: String) {
    val ts = System.currentTimeMillis
    Log.d(LOG_TAG, " *** " + method + " " + className + " " + ts)
    if (enableNotifications) {
      val notification = new Notification(android.R.drawable.star_big_on, "Lifeycle Event: " + method, 0L)
      val notificationContentView = new RemoteViews(getPackageName, R.layout.custom_notification_layout)
      notification.contentView = notificationContentView
      notification.contentIntent = PendingIntent.getActivity(this, 0, null, 0)
      notification.flags |= Notification.FLAG_AUTO_CANCEL
      notificationContentView.setImageViewResource(R.id.image, android.R.drawable.btn_star)
      notificationContentView.setTextViewText(R.id.lifecycle_class, className)
      notificationContentView.setTextViewText(R.id.lifecycle_method, method)
      notificationContentView.setTextColor(R.id.lifecycle_method, R.color.black)
      notificationContentView.setTextViewText(R.id.lifecycle_timestamp, ts.toString)
      notifyMgr.notify(System.currentTimeMillis.toInt, notification)
    }
  }
}

object LifecycleActivity {
  final val LOG_TAG = "LifecycleExplorer"
}
