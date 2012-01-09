package com.manning.aip.location

import android.location.{Criteria, Location, LocationListener, LocationManager, LocationProvider }
import android.os.{Bundle, Handler, Message}
import android.util.Log

/**
 * Helper class to encapsulate some of the common code needed to determine the
 * current location using FINE (GPS) provider. 
 * <p/>
 * If the most recent location is available for the FINE provider, and it is relatively
 * recent (within FIX_RECENT_BUFFER_TIME -- currently 30 seconds), it is returned back to
 * the caller using a Message indicating the results. 
 * <p/>
 * IF the most recent location is either not available, or too old to be used, the
 * a LocationListener is kicked off for a specified duration. Once the LocationListener 
 * either gets a good Location update, or the time is elapsed, a Message is sent back 
 * to the caller indicating the results.
 * <p/>
 * Example usage from an Activity:
 * <p/>
 * <pre>
 *   val handler = new Handler() {
 *     def handleMessage(m: Message) {
 *       Log.d(LOG_TAG, "Handler returned with message: " + m.toString)
 *       if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_FOUND) {
 *         Toast.makeText(Activity.this, "HANDLER RETURNED -- lat:" + m.arg1 + " lon:" + m.arg2, Toast.LENGTH_SHORT)
 *                       .show()
 *       } else if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_NULL) {
 *         Toast.makeText(Activity.this, "HANDLER RETURNED -- unable to get location", Toast.LENGTH_SHORT).show()
 *       } else if (m.what == LocationHelper.MESSAGE_CODE_PROVIDER_NOT_PRESENT) {
 *         Toast.makeText(Activity.this, "HANDLER RETURNED -- provider not present", Toast.LENGTH_SHORT).show()
 *       }
 *     }
 *   }
 *     
 *   val helper = new LocationHelper(locationManager, handler, LOG_TAG)
 *   helper getCurrentLocation handler
 * </pre> 
 * 
 * @author ccollins
 */
class LocationHelper(locationMgr: LocationManager, handler: Handler, logTag: String) {
  import LocationHelper._  // companion object

  private val locationListener: LocationListener = new LocationListenerImpl()
  private val handlerCallback: Runnable = new Thread() {
    override def run() { endListenForLocation(null) }
  }
  private val providerName: String = {
    val criteria = new Criteria()
    // use Criteria to get provider (and could use COARSE, but doesn't work in emulator)
    // (FINE will use EITHER network/gps, whichever is the best enabled match, except in emulator must be gps)
    // (NOTE: network won't work unless enabled - Settings->Location & Security Settings->Use wireless networks)
    criteria.setAccuracy(Criteria.ACCURACY_FINE)
    locationMgr.getBestProvider(criteria, true)
  }

  /**
   * Invoke the process of getting the current Location.
   * Expect Messages to be returned via the Handler passed in at construction with results.
   * 
   * @param durationSeconds amount of time to poll for location updates
   */
  def getCurrentLocation(durationSeconds: Int) {
    if (this.providerName == null) {
      // return 2/0/0 if provider is not enabled
      Log.d(logTag, "Location provideName null, provider is not enabled or not present.")
      sendLocationToHandler(MESSAGE_CODE_PROVIDER_NOT_PRESENT, 0, 0)
      return
    }

    // first check last KNOWN location (and if the fix is recent enough, use it)
    // NOTE -- this does NOT WORK in the Emulator
    // (if you send a DDMS "manual" time or geo fix, you get correct DATE, 
    // but fix time starts at 00:00 and seems to increment by 1 second each time sent)
    // to test this section (getLastLocation being recent enough), you need to use a real device
    val lastKnown = locationMgr getLastKnownLocation providerName
    if (lastKnown != null && lastKnown.getTime >= (System.currentTimeMillis - FIX_RECENT_BUFFER_TIME)) {
      Log.d(logTag, "Last known location recent, using it: " + lastKnown.toString)
      // return lastKnown lat/long on Message via Handler
      sendLocationToHandler(MESSAGE_CODE_LOCATION_FOUND,
                            (lastKnown.getLatitude * 1e6).toInt,
                            (lastKnown.getLongitude * 1e6).toInt)
    } else {
      // last known is relatively old, or doesn't exist, use a LocationListener 
      // and wait for a location update for X seconds
      Log.d(logTag, "Last location NOT recent, setting up location listener to get newer update.")
      listenForLocation(providerName, durationSeconds)
    }
  }

  private def sendLocationToHandler(msgId: Int, lat: Int, lon: Int) {
    val msg = Message.obtain(handler, msgId, lat, lon)
    handler sendMessage msg
  }

  private def listenForLocation(providerName: String, durationSeconds: Int) {
    locationMgr.requestLocationUpdates(providerName, 0, 0, locationListener)
    handler.postDelayed(handlerCallback, durationSeconds * 1000)
  }

  private def endListenForLocation(loc: Location) {
    locationMgr removeUpdates locationListener
    handler removeCallbacks handlerCallback
    val (lat, lon) =
      if (loc != null) ((loc.getLatitude * 1e6).toInt, (loc.getLongitude * 1e6).toInt)
      else (0, 0)
    sendLocationToHandler(MESSAGE_CODE_LOCATION_FOUND, lat, lon)
  }

  private class LocationListenerImpl extends LocationListener {
    override def onStatusChanged(provider: String, status: Int, extras: Bundle) {
      Log.d(logTag, "Location status changed to:" + status)
      status match {
        case LocationProvider.AVAILABLE =>
        case LocationProvider.TEMPORARILY_UNAVAILABLE =>
        case LocationProvider.OUT_OF_SERVICE => endListenForLocation(null)
      }
    }

    override def onLocationChanged(loc: Location) {         
      if (loc == null) return

      Log.d(logTag, "Location changed to:" + loc.toString)
      endListenForLocation(loc)
    }

    override def onProviderDisabled(provider: String) {
      endListenForLocation(null)
    }

    override def onProviderEnabled(provider: String) {
    }
  }
}

object LocationHelper {
  final val MESSAGE_CODE_LOCATION_FOUND = 1
  final val MESSAGE_CODE_LOCATION_NULL = 2
  final val MESSAGE_CODE_PROVIDER_NOT_PRESENT = 3

  private final val FIX_RECENT_BUFFER_TIME = 30000
}
