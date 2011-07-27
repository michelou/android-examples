package com.manning.aip.portfolio

import android.content.{BroadcastReceiver, Context, Intent}

/**
 * `BroadcastReceiver` used to receive events from the
 * Cloud to Device Messaging (C2DM) service. 
 * 
 * @author Michael Galpin
 *
 */
class PushReceiver extends BroadcastReceiver {
  import PushReceiver._  // companion object

  override def onReceive(context: Context, intent: Intent) {
    AlarmReceiver acquireLock context
    if (intent.getAction equals REGISTRATION) {
      onRegistration(context, intent)
    } else if (intent.getAction equals RECEIVE) {
      onMessage(context, intent)
    }
  }
  
  /**
   * This method is used to handle the registration event being sent from
   * C2DM.
   * 
   * @param   context      The `Context` for the event
   * @param   intent      The `Intent` received from C2DM
   */
  private def onRegistration(context: Context, intent: Intent) {
    val regId = intent getStringExtra "registration_id"
    if (regId != null) {
      val i = new Intent(context, classOf[SendC2dmRegistrationService])
      i.putExtra("regId", regId)
      context startService i
    }
  }
  
  /**
   * This method is used to handle events sent from your servers, via the
   * C2DM service.
   * 
   * @param   context      The `Context` for the event
   * @param   intent      The `Intent` received from C2DM
   */
  private def onMessage(context: Context, intent: Intent){
    val stockService = new Intent(context, classOf[PortfolioManagerService])
    // copy any data sent from your server
    stockService putExtras intent
    context startService stockService
  }
}

object PushReceiver {
  private val REGISTRATION = "com.google.android.c2dm.intent.REGISTRATION"
  private val RECEIVE = "com.google.android.c2dm.intent.RECEIVE"
}

