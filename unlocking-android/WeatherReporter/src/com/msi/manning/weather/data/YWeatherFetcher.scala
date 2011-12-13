package com.msi.manning.weather
package data

import android.util.Log

import org.xml.sax.{InputSource, XMLReader}

import java.net.URL

import javax.xml.parsers.{SAXParser, SAXParserFactory}

/**
 * Invoke Yahoo! Weather API and parse into WeatherRecord.
 * 
 * @see YWeatherHandler
 * 
 * @author charliecollins
 * 
 */
class YWeatherFetcher(zip: String, overrideSevere: Boolean) {
  import YWeatherFetcher._  // companion object

  private val query: String = QBASE + zip
  // /Log.v(Constants.LOGTAG, " " + CLASSTAG + " query - " + query)

  // validate location is a zip
  if (zip == null || zip.length != 5 || !isNumeric(zip))
    throw new IllegalArgumentException("invalid zip")

  def this(zip: String) = this(zip, false)

  def getWeather: WeatherRecord = {
    // val start = System.currentTimeMillis
    var r = new WeatherRecord()

    try {
      val url = new URL(query)
      val spf = SAXParserFactory.newInstance()
      val sp = spf.newSAXParser()
      val xr = sp.getXMLReader()
      val handler = new YWeatherHandler()
      xr setContentHandler handler
      xr parse new InputSource(url.openStream())
      // after parsed, get record
      r = handler.getWeatherRecord
      r setOverrideSevere true // override severe for dev/testing
    } catch {
      case e: Exception =>
        Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
    }

    // val duration = (System.currentTimeMillis - start) / 1000
    // Log.v(Constants.LOGTAG, " " + CLASSTAG + " call duration - " + duration)
    // Log.v(Constants.LOGTAG, " " + CLASSTAG + " WeatherReport = " + r)
    r
  }

}

object YWeatherFetcher {
  private final val CLASSTAG = classOf[YWeatherFetcher].getSimpleName
  private final val QBASE = "http://weather.yahooapis.com/forecastrss?p="

  private def isNumeric(s: String): Boolean =
    try { s.toInt; true }
    catch { case _ => false }

  def getMockRecord: WeatherRecord = {
    val r = new WeatherRecord()
    r setCity "Crested Butte"
    r setCondition WeatherCondition.SUNNY
    r setCountry "US"
    r setDate "03-08-2008"
    val w1 = new WeatherForecast()
    w1 setCondition WeatherCondition.HEAVY_SNOW_WINDY
    w1 setDate "03-09-2008"
    w1 setDay "Sun"
    w1 setHigh 22
    w1 setLow 3
    val w2 = new WeatherForecast()
    w2 setCondition WeatherCondition.FAIR_DAY
    w2 setDate "03-10-2008"
    w2 setDay "Mon"
    w2 setHigh 28
    w2 setLow 5
    r setForecasts Array(w1, w2)
    r setHumidity 100
    r setLink "link"
    r setPressure 30.4
    r setPressureState WeatherRecord.PRESSURE_RISING
    r setRegion "CO"
    r setSunrise "6:27 am"
    r setSunset "6:11 pm"
    r setTemp 11
    r setVisibility 250
    r setWindChill "-12"
    r setWindDirection "NNE"
    r setWindSpeed 23
    r
  }

}
