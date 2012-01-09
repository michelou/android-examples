package com.manning.aip.location

import android.app.{Activity, AlertDialog}
import android.content.{Context, DialogInterface, Intent}
import android.location.{GpsSatellite, GpsStatus, LocationManager}
import android.os.{Bundle, Handler, Message}
import android.provider.Settings
import android.util.Log
import android.widget.TextView

class GetLocationWithGPS extends Activity {
  import GetLocationWithGPS._  // companion object

  private var locationMgr: LocationManager = _
  private var gpsListener: GpsListener = _
  private var gpsStatus: GpsStatus = _
  private var handler: Handler = _

  private var title: TextView = _
  private var detail: TextView = _
  private var gpsEvents: TextView = _
  private var satelliteStatus: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.get_location)

    title = findViewById(R.id.title).asInstanceOf[TextView]
    detail = findViewById(R.id.detail).asInstanceOf[TextView]
    gpsEvents = findViewById(R.id.gps_events).asInstanceOf[TextView]
    satelliteStatus = findViewById(R.id.satellite_status).asInstanceOf[TextView]

    title setText "Get current location via GPS"
    detail setText "Attempting to get current location...\n     (may take a few seconds)"

    locationMgr = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    gpsListener = new GpsListener()

    handler = new Handler() {
      override def handleMessage(m: Message) {
        Log.d(Main.LOG_TAG, "Handler returned with message: " + m.toString)
        if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_FOUND)
          detail setText ("HANDLER RETURNED\nlat:" + m.arg1 + "\nlon:" + m.arg2)
        else if (m.what == LocationHelper.MESSAGE_CODE_LOCATION_NULL)
          detail setText "HANDLER RETURNED\nunable to get location"
        else if (m.what == LocationHelper.MESSAGE_CODE_PROVIDER_NOT_PRESENT)
          detail setText "HANDLER RETURNED\nprovider not present"
      }
    }
  }

  override protected def onResume() {
    super.onResume()

    // determine if GPS is enabled or not, if not prompt user to enable it
    if (!locationMgr.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
      val builder = new AlertDialog.Builder(this)
      builder.setTitle("GPS is not enabled")
             .setMessage("Would you like to go the location settings and enable GPS?").setCancelable(true)
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               def onClick(dialog: DialogInterface, id: Int) {
                 startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS))
               }
             })
             .setNegativeButton("No", new DialogInterface.OnClickListener() {
               def onClick(dialog: DialogInterface, id: Int) {
                 dialog.cancel()
                 finish()
               }
             })
      val alert = builder.create()
      alert.show()
    } else {
      val locationHelper = new LocationHelper(locationMgr, handler, Main.LOG_TAG)
      // here we aren't using a progressdialog around getCurrentLocation
      // (don't want to block entire UI)
      // (but be advised that you could if the situation absolutely required it)
      locationHelper getCurrentLocation 30
    }

    locationMgr addGpsStatusListener gpsListener
  }

  override protected def onPause() {
    super.onPause()
    locationMgr removeGpsStatusListener gpsListener
  }

  // you can also use a GpsListener to be notified when the GPS is
  // started/stopped, and when first "fix" is obtained
  private class GpsListener extends GpsStatus.Listener {
    def onGpsStatusChanged(event: Int) {
      Log.d("GpsListener", "Status changed to " + event)
      event match {
        case GpsStatus.GPS_EVENT_STARTED =>
          gpsEvents setText "GPS_EVENT_STARTED"
        case GpsStatus.GPS_EVENT_STOPPED =>
          gpsEvents setText "GPS_EVENT_STOPPED"
          // GPS_EVENT_SATELLITE_STATUS will be called frequently
          // all satellites in use will invoke it, don't rely on it alone
        case GpsStatus.GPS_EVENT_SATELLITE_STATUS =>
          // this is *very* chatty, only very advanced use cases should need
          // this (avoid it if you don't need it)
          gpsStatus = locationMgr getGpsStatus gpsStatus
          val sb = new StringBuilder()
          import scala.collection.JavaConversions._
          val satellites = iterableAsScalaIterable(gpsStatus.getSatellites)
          for (sat <- satellites) {
            sb.append("Satellite N:" + sat.getPrn +
                      " AZ:" + sat.getAzimuth +
                      " EL:" + sat.getElevation +
                      " S/N:" + sat.getSnr + "\n")
          }
          satelliteStatus setText sb.toString
        case GpsStatus.GPS_EVENT_FIRST_FIX =>
          gpsEvents setText "GPS_EVENT_FIRST_FIX"
      }
    }
  }
}

object GetLocationWithGPS {
  final val LOC_DATA = "LOC_DATA"
}
