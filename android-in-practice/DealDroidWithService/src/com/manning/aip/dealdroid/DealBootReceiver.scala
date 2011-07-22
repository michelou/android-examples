package com.manning.aip.dealdroid

import android.app.{AlarmManager, PendingIntent}
import android.content.{BroadcastReceiver, Context, Intent}
import android.util.Log

class DealBootReceiver extends BroadcastReceiver {

  override def onReceive(context: Context, intent: Intent) {
    Log.i(Constants.LOG_TAG, "DealBootReceiver invoked, configuring AlarmManager")
    val alarmMgr =
      context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val pendingIntent = PendingIntent.getBroadcast(context, 0,
      new Intent(context, classOf[DealAlarmReceiver]), 0)

    // use inexact repeating which is easier on battery
    // (system can phase events and not wake at exact times)
    alarmMgr.setInexactRepeating(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      Constants.ALARM_TRIGGER_AT_TIME,
      Constants.ALARM_INTERVAL,
      pendingIntent)
  }
}
