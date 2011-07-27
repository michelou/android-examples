package com.manning.aip.portfolio

import android.content.{BroadcastReceiver, Context, Intent}
import android.os.PowerManager

/**
 * A [[http://developer.android.com/reference/android/content/BroadcsatReceiver.html BraodcastReceiver]] 
 * that is notified when a system alarm fires. It then starts the 
 * [[com.manning.aip.portfolio.PortfolioManagerService PortfolioManagerServic]]
 * passing it a [[http://developer.android.com/reference/android/os/PowerManager.WakeLock.html WakeLock]].
 * 
 * @author Michael Galpin
 *
 */
class AlarmReceiver extends BroadcastReceiver {
  import AlarmReceiver._  // companion object

  /* (non-Javadoc)
   * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
   */
  override def onReceive(context: Context, intent: Intent) {
    acquireLock(context)
    val stockService = new Intent(context, classOf[PortfolioManagerService])
    context startService stockService
  }

}

object AlarmReceiver {
  private var wakeLock: PowerManager#WakeLock = null
  private final val LOCK_TAG = "com.manning.aip.portfolio"
  
  /**
   * Method used to share the `WakeLock` created by this 
   * <code>BroadcastReceiver</code>. 
   * Note: this method is <code>synchronized</code> as it lazily creates
   * the <code>WakeLock</code>
   * 
   * @param   ctx  The `Context` object acquiring the lock.
   */
  def acquireLock(ctx: Context) = synchronized {
    if (wakeLock == null) {
      val mgr = ctx.getSystemService(Context.POWER_SERVICE).asInstanceOf[PowerManager]
      wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_TAG)
      wakeLock setReferenceCounted true
    }
    wakeLock.acquire()
  }

  /**
   * Method used to release the shared <code>WakeLock</code>.
   */
  def releaseLock() = synchronized {
    if (wakeLock != null)
      wakeLock.release()
  }
}

