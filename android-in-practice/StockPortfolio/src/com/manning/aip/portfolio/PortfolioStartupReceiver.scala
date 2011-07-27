package com.manning.aip.portfolio

import java.util.Calendar

import android.app.{AlarmManager, PendingIntent}
import android.content.{BroadcastReceiver, Context, Intent}

/**
 * <code>BroadcastReceiver</code> that is notified when the device boots up.
 * It uses the {@link http://developer.android.com/reference/android/app/AlarmManager.html AlarmManager}
 * to schedule regular invocations of the 
 * {@link com.manning.aip.portfolio.PorfolioManagerService PortfolioManagerService}.
 * 
 * This can be easily modified to use Cloud to Device Messaging, see comments in
 * the code for details.
 * 
 * @author Michael Galpin
 *
 */
class PortfolioStartupReceiver extends BroadcastReceiver {
  import PortfolioStartupReceiver._  // companion object

  override def onReceive(context: Context, intent: Intent) {
    
  // Begin AlarmManager code. Delete this to use C2DM
  val mgr =
    context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
  val i = new Intent(context, classOf[AlarmReceiver])
  val sender = PendingIntent.getBroadcast(context, 0, 
        i, PendingIntent.FLAG_CANCEL_CURRENT)
  val now = Calendar.getInstance
  now.add(Calendar.MINUTE, 2)
  mgr.setRepeating(AlarmManager.RTC_WAKEUP, 
        now.getTimeInMillis(), FIFTEEN_MINUTES, sender)
  // End AlarmManager code
    
  // Uncomment out the following code to use C2DM
//  val registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER")
//  registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0)) // boilerplate
//  registrationIntent.putExtra("sender", DEVELOPER_EMAIL_ADDRESS)
//  context startService registrationIntent
  }
}

object PortfolioStartupReceiver {
  private final val FIFTEEN_MINUTES = 15*60*1000

  // Uncomment this constant to use C2DM and insert the email address that
  // you use to submit your apps to the Android Market
  //private final val DEVELOPER_EMAIL_ADDRESS = ""

}

