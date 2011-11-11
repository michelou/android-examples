package com.msi.manning.weather
package service

import android.content.{BroadcastReceiver, Context, Intent}
import android.util.Log

class WeatherAlertServiceReceiver extends BroadcastReceiver {
  import WeatherAlertServiceReceiver._  // companion object

  override def onReceive(context: Context, intent: Intent) {
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      Log.v(Constants.LOGTAG, " " + CLASSTAG +
            " received intent via BOOT, starting service")
      context startService new Intent(context, classOf[WeatherAlertService])
    }
  }
}

object WeatherAlertServiceReceiver {
  private final val CLASSTAG = classOf[WeatherAlertServiceReceiver].getSimpleName
}
