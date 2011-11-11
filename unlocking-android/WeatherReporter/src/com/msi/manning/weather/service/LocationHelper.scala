package com.msi.manning.weather.service

import android.location.Criteria

class LocationHelper

object LocationHelper {

  private final val CLASSTAG = classOf[LocationHelper].getSimpleName

  final val PROVIDER_CRITERIA = new Criteria()
  PROVIDER_CRITERIA setAccuracy Criteria.NO_REQUIREMENT
  PROVIDER_CRITERIA setAltitudeRequired false
  PROVIDER_CRITERIA setBearingRequired false
  PROVIDER_CRITERIA setCostAllowed false
  PROVIDER_CRITERIA setPowerRequirement Criteria.NO_REQUIREMENT
  PROVIDER_CRITERIA setSpeedRequired false

}
