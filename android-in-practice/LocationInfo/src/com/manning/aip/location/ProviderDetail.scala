package com.manning.aip.location

import android.app.Activity
import android.content.Context
import android.location.{GpsSatellite, GpsStatus}
import android.location.{Location, LocationManager, LocationProvider}
import android.os.Bundle
import android.util.{Printer, StringBuilderPrinter}
import android.widget.TextView

// NOTE that "network" provider will always return null for getLastKnownLocation
// if settings->location and security->Use wireless networks is NOT CHECKED (very often it's not)

class ProviderDetail extends Activity {
  private var locationMgr: LocationManager = _

  private var title: TextView = _
  private var detail: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.title_detail)

    locationMgr = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    title = findViewById(R.id.title).asInstanceOf[TextView]
    detail = findViewById(R.id.detail).asInstanceOf[TextView]
  }

  override protected def onResume() {
    super.onResume()

    val providerName = getIntent getStringExtra "PROVIDER_NAME"
    val lastLocation = locationMgr getLastKnownLocation providerName
    val provider = locationMgr getProvider providerName

    val sb = new java.lang.StringBuilder()

    sb append "location manager data"
    sb append "\n--------------------------------"
    if (lastLocation != null) {
      sb append "\n"
      val printer = new StringBuilderPrinter(sb)
      lastLocation.dump(printer, "last location: ")
    } else
      sb append "\nlast location: null\n"

    sb append "\n"
    sb append "\nprovider properties"
    sb append "\n--------------------------------"
    sb append "\naccuracy: " append provider.getAccuracy
    sb append "\npower requirement: " append provider.getPowerRequirement
    sb append "\nhas monetary cost: " append provider.hasMonetaryCost
    sb append "\nsupports altitude: " append provider.supportsAltitude
    sb append "\nsupports bearing: " append provider.supportsBearing
    sb append "\nsupports speed: " append provider.supportsSpeed
    sb append "\nrequires cell: " append provider.requiresCell
    sb append "\nrequires network: " append provider.requiresNetwork
      
    // extra details for GpsStatus if provider is GPS
    if (providerName equalsIgnoreCase LocationManager.GPS_PROVIDER) {
      val gpsStatus = locationMgr getGpsStatus null
      sb append "\ngps status"
      sb append "\n--------------------------------"
      sb append "\ntime to first fix: " append gpsStatus.getTimeToFirstFix
      sb append "\nmax satellites: " append gpsStatus.getMaxSatellites

      import scala.collection.JavaConversions._
      val satellites = iterableAsScalaIterable(gpsStatus.getSatellites)
      sb append "\ncurrent satellites: " append satellites.size
      for (satellite <- satellites) {
        sb append "\nsatellite: " append satellite.getPrn
        sb append "\n   azimuth " append satellite.getAzimuth
        sb append "\n   elevation " append satellite.getElevation
        sb append "\n   signal to noise ratio " append satellite.getSnr
      }
    }

    title setText ("Provider: " + providerName)
    detail setText sb.toString
  }
}
