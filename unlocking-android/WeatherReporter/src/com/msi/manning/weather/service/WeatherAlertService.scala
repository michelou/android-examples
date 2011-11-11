package com.msi.manning.weather
package service

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.{Context, Intent}
import android.location.{Address, Geocoder, LocationListener, LocationManager}
import android.net.Uri
import android.os.{Bundle, Handler, IBinder, Message}
import android.util.Log

import data.{DBHelper, WeatherRecord, YWeatherFetcher}
import DBHelper.Location

import java.io.IOException
import java.util.{Timer, TimerTask}

import scala.collection.JavaConversions._

/**
 * Background service to check for severe weather for specific locations and
 * alert user.
 * 
 * Note that this is started at BOOT (in which case onCreate and onStart are
 * called), and is bound from within ReportDetail Activity in WeatherReporter
 * application. This Service is started in the background for alert processing
 * (standalone), bound in Activities to call methods on Binder to register
 * alert locations.
 * 
 * @author charliecollins
 * 
 */
class WeatherAlertService extends Service {
  import WeatherAlertService._  // companion object

  private var timer: Timer = _
  private var dbHelper: DBHelper = _
  private var nm: NotificationManager = _

  private val task = new TimerTask() {

    override def run() {
      // poll user specified locations
      val locations = dbHelper.getAllAlertEnabled
      for (loc <- locations) {
        val record = loadRecord(loc.zip)
        if (record.isSevere) {
          if ((loc.lastalert + ALERT_QUIET_PERIOD) < System.currentTimeMillis) {
            loc.lastalert = System.currentTimeMillis
            dbHelper update loc
            sendNotification(loc.zip, record)
          }
        }
      }

      // poll device location
      val deviceAlertEnabledLoc = dbHelper.get(DBHelper.DEVICE_ALERT_ENABLED_ZIP)
      if (deviceAlertEnabledLoc != null) {
        val record = loadRecord(deviceLocationZIP)
        if (record.isSevere) {
          if ((deviceAlertEnabledLoc.lastalert + ALERT_QUIET_PERIOD) < System
                        .currentTimeMillis) {
            deviceAlertEnabledLoc.lastalert = System.currentTimeMillis
            dbHelper update deviceAlertEnabledLoc
            sendNotification(WeatherAlertService.deviceLocationZIP, record)
          }
        }
      }
    }
  }

  private val handler = new Handler() {
    override def handleMessage(msg: Message) {
      notifyFromHandler(msg.getData.get(LOC).toString, msg.getData.get(ZIP).toString)
    }
  }

  override def onCreate() {
    dbHelper = new DBHelper(this)
    timer = new Timer()
    timer.schedule(task, 5000, ALERT_POLL_INTERVAL)
    nm = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
  }

  override def onStart(intent: Intent, startId: Int) {
    super.onStart(intent, startId)

    Log.v(Constants.LOGTAG, " " + CLASSTAG + "   onStart")

    val locMgr = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    val geocoder = new Geocoder(this)

    val locListener = new LocationListener() {

      def onLocationChanged(loc: android.location.Location) {
        val lati = loc.getLatitude
        val longi = loc.getLongitude
        Log.v(Constants.LOGTAG, " " + CLASSTAG +
              "   locationProvider LOCATION CHANGED lat/long - " +
              lati + " " + longi)
        try {
          val addresses = geocoder.getFromLocation(lati, longi, 5)
          if (addresses != null) {
            var found = false
            for (a <- addresses if !found) {
              Log.w(Constants.LOGTAG, " " + CLASSTAG +
                    "   parsing address for geocode ZIP - country:" + a.getCountryCode +
                    " locality:" + a.getLocality + " postalCode:" + a.getPostalCode)
              if (a.getPostalCode != null) {
                deviceLocationZIP = addresses.get(0).getPostalCode
                Log.v(Constants.LOGTAG, " " + CLASSTAG +
                      "   updating deviceLocationZIP to " + deviceLocationZIP)
                found = true
              }
            }
            Log.v(Constants.LOGTAG, " " + CLASSTAG +
                  "   after parsing all geocode addresses deviceLocationZIP = "
                            + deviceLocationZIP)
          } else {
            Log.v(Constants.LOGTAG, " " + CLASSTAG +
                  "   NOT updating deviceLocationZIP, geocode addresses NULL")
          }
        } catch {
          case e: IOException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        }
      }

      def onProviderDisabled(s: String) {
        Log.v(Constants.LOGTAG, " " + CLASSTAG + "   locationProvider DISABLED - " + s)
      }
      def onProviderEnabled(s: String) {
        Log.v(Constants.LOGTAG, " " + CLASSTAG + "   locationProvider ENABLED - " + s)
      }
      def onStatusChanged(s: String, i: Int, b: Bundle) {
        Log.v(Constants.LOGTAG, " " + CLASSTAG + "   locationProvider STATUS CHANGE - " + s)
      }
    }

    // we set minTime and minDistance to 0 here to get updated ALL THE TIME ALWAYS
    // in real life you DO NOT want to do this, it will consume too many resources
    // see LocationMangaer in JavaDoc for guidelines (time less than 60000 is not recommended)
    val locProvider = locMgr.getBestProvider(LocationHelper.PROVIDER_CRITERIA, true)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + "   locationProvider - " + locProvider)
    if (locProvider != null) {
      locMgr.requestLocationUpdates(locProvider, 0, 0, locListener)
    } else {
      Log.e(Constants.LOGTAG, " " + CLASSTAG + "  NO LOCATION PROVIDER AVAILABLE")
    }
  }

  override def onDestroy() {
    super.onDestroy()
    dbHelper.cleanup()
  }

  override def onBind(intent: Intent): IBinder = null

  private def loadRecord(zip: String): WeatherRecord = {
    val ywh = new YWeatherFetcher(zip, true)
    ywh.getWeather
  }

  private def sendNotification(zip: String, record: WeatherRecord) {
    val message = Message.obtain()
    val bundle = new Bundle()
    bundle.putString(ZIP, zip)
    bundle.putString(LOC, record.getCity + ", " + record.getRegion)
    message setData bundle
    handler sendMessage message
  }

  private def notifyFromHandler(location: String, zip: String) {
    val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + zip)
    val intent = new Intent(Intent.ACTION_VIEW, uri)
    val pendingIntent = PendingIntent.getActivity(this, Intent.FLAG_ACTIVITY_NEW_TASK, intent,
            PendingIntent.FLAG_ONE_SHOT)
    val n = new Notification(R.drawable.severe_weather_24, "Severe Weather Alert!", System
            .currentTimeMillis)
    n.setLatestEventInfo(this, "Severe Weather Alert!", location, pendingIntent)
    nm.notify(zip.toInt, n)
  }
}

object WeatherAlertService {
  private final val CLASSTAG = classOf[WeatherAlertService].getSimpleName
  private final val LOC = "LOC"
  private final val ZIP = "ZIP"
  private final val ALERT_QUIET_PERIOD = 10000
  private final val ALERT_POLL_INTERVAL = 15000

  // convenience for Activity classes in the same process to get current device location
  // (so they don't have to repeat all the LocationManager and provider stuff locally)
  // (this would NOT work across applications, only for things in the same PROCESS)
  private[weather] var deviceLocationZIP = "94102"
}
