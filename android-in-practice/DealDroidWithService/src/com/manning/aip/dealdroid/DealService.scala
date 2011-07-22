package com.manning.aip.dealdroid

import android.app.{IntentService, Notification, NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.util.Log

import java.util.{ArrayList => JArrayList, List => JList}

import model.Section

// Use IntentService which will queue each call to startService(Intent)
// through onHandleIntent and then shutdown
//
// NOTE that this implementation intentionally doesn't use PowerManager/WakeLock or deal with power issues
// (if the device is asleep, AlarmManager wakes up for BroadcastReceiver onReceive, but then might sleep again)
// (can use PowerManager and obtain WakeLock here, but STILL might not work, there is a gap)
// (this can be mitigated but for this example this complication is not needed)
// (it's not critical if user doesn't see new deals until phone is awake and notification is sent, both)
class DealService extends IntentService("Deal Service") {
  import scala.collection.JavaConversions._

  private var app: DealDroidApp = _
  private var sections: JList[Section] = _
 
  override def onStart(intent: Intent, startId: Int) {
    super.onStart(intent, startId)
  }

  override def onHandleIntent(intent: Intent) {
    Log.i(Constants.LOG_TAG, "DealService invoked, checking for new deals (will notify if present)")
    this.app = getApplication.asInstanceOf[DealDroidApp]
    this.sections = app.getSectionList
    if (app.connectionPresent) {
      // parse the feed
      sections.clear()
      sections addAll app.getParser.parse()

      // get list of currentDealIds from first section (Daily Deals, always 4 items)
      val currentDealIds = app parseItemsIntoDealIds sections.get(0).items

      // previous deals - stored as prefs because it's easier than files for
      // simple data and we need something persistent when service wakes up
      // (previous app memory may not still be around)
      val previousDealIds = app.getPreviousDealIds

      // store currentDealIds as PREVIOUS so we're up to date next time around
      app setPreviousDealIds currentDealIds

      // do we have any NEW ids?
      val newDealIdsList = this.checkForNewDeals(previousDealIds, currentDealIds)
      if (!newDealIdsList.isEmpty) {
        this.sendNotification(this, newDealIdsList.size)
      }

      // uncomment to force notification, new deals or not
      /*
      count += 1
      if (count == 1) {
        SystemClock sleep 5000
        this.sendNotification(this, 1)
      }
      */
    } else {
      Log.w(Constants.LOG_TAG, "Network connection not available, not checking for new deals");
    }
  }

  // instead of using entire Item, use itemId, it's unique enough to know what's new         
  private def checkForNewDeals(previousDealIds: JList[Long], currentDealIds: JList[Long]): JList[Long] = {
    val newDealsList = new JArrayList[Long]
    for (id <- currentDealIds) {
      if ((id != 0) && !previousDealIds.contains(id)) {
        Log.d(Constants.LOG_TAG, "New deal found: " + id)
        newDealsList add id
      }
    }
    newDealsList
  }

  private def sendNotification(context: Context, numNewDeals: Int) {
    val notificationIntent = new Intent(context, classOf[DealList])
    notificationIntent.putExtra(Constants.FORCE_RELOAD, true)
    val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

    val notificationMgr =
      context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val notification =
       new Notification(android.R.drawable.star_on,
                        getString(R.string.deal_service_ticker),
                        System.currentTimeMillis)
    notification.flags |= Notification.FLAG_AUTO_CANCEL
    notification.setLatestEventInfo(
      context,
      getResources.getString(R.string.deal_service_title),
      getResources.getQuantityString(R.plurals.deal_service_new_deal, numNewDeals, numNewDeals.asInstanceOf[AnyRef]),
      contentIntent)
    notificationMgr.notify(0, notification)
  }
}
