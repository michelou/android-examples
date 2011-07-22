package com.manning.aip.dealdroid

import android.content.{BroadcastReceiver, Context, Intent}
import android.util.Log

class DealAlarmReceiver extends BroadcastReceiver {

  // onReceive must be very quick and not block, so it just fires up a Service
  override def onReceive(context: Context, intent: Intent) {
    Log.i(Constants.LOG_TAG, "DealAlarmReceiver invoked, starting DealService in background")
    context startService new Intent(context, classOf[DealService])
  }
}
