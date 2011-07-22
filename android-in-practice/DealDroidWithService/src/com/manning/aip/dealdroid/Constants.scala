package com.manning.aip.dealdroid

import android.app.AlarmManager
import android.os.SystemClock

object Constants {

  final val LOG_TAG = "DealDroid"

  final val FORCE_RELOAD = "FORCE_RELOAD"

  // In real life, use AlarmManager.INTERVALs with longer periods of time for
  // dev you can shorten this to 10000 or such, but deals don't change often
  // anyway (better yet, allow user to set and use PreferenceActivity)
  ///final val ALARM_INTERVAL = 10000
  final val ALARM_INTERVAL = AlarmManager.INTERVAL_HOUR
  final val ALARM_TRIGGER_AT_TIME = SystemClock.elapsedRealtime + 30000

  // for SharedPrefernces keys for current deals (so we can compare and know
  // if we have new deals)
  final val DEAL1 = "deal1"
  final val DEAL2 = "deal2"
  final val DEAL3 = "deal3"
  final val DEAL4 = "deal4"
  final val DEAL5 = "deal5"  // there usually isn't a deal 5, but an extra in case
}
