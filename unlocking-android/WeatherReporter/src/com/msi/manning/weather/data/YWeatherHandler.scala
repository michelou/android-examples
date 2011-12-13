package com.msi.manning.weather.data

import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

/**
 * SAX Handler impl for Yahoo! Weather API and WeatherRecord bean.
 * 
 * @author charliecollins
 * 
 */
class YWeatherHandler extends DefaultHandler {
  import YWeatherHandler._  // companion object

  private var forecastCount: Int = _
  private var weatherRecord: WeatherRecord = new WeatherRecord()

  @throws(classOf[SAXException])
  override def startDocument() {}

  @throws(classOf[SAXException])
  override def endDocument() {}

  @throws(classOf[SAXException])
  override def startElement(namespaceURI: String, localName: String,
                            qName: String, atts: Attributes) {
    localName match {
      case YWeatherHandler.YLOC =>
        weatherRecord setCity getAttributeValue("city", atts)
        weatherRecord setRegion getAttributeValue("region", atts)
        weatherRecord setCountry getAttributeValue("country", atts)

      case YWeatherHandler.YWIND =>
        weatherRecord setWindChill getAttributeValue("chill", atts)
        val windDirectionDegrees = getAttributeValue("direction", atts).toInt
        weatherRecord setWindDirection convertDirection(windDirectionDegrees)
        weatherRecord setWindSpeed getAttributeValue("speed", atts).toInt

      case YWeatherHandler.YATMO =>
        weatherRecord setHumidity getAttributeValue("humidity", atts).toInt
        weatherRecord setVisibility getAttributeValue("visibility", atts).toInt
        weatherRecord setPressure getAttributeValue("pressure", atts).toDouble
        val pressureState = getAttributeValue("rising", atts)
        pressureState match {
          case "0" => weatherRecord setPressureState WeatherRecord.PRESSURE_STEADY
          case "1" => weatherRecord setPressureState WeatherRecord.PRESSURE_FALLING
          case "2" => weatherRecord setPressureState WeatherRecord.PRESSURE_RISING
          case _ =>
        }

      case YWeatherHandler.YASTRO =>
        weatherRecord setSunrise getAttributeValue("sunrise", atts)
        weatherRecord setSunset getAttributeValue("sunset", atts)

      case YWeatherHandler.YCOND =>
        weatherRecord setTemp getAttributeValue("temp", atts).toInt
        val code = getAttributeValue("code", atts).toInt
        val cond = WeatherCondition.getWeatherCondition(code)
        weatherRecord setCondition cond
        weatherRecord setDate getAttributeValue("date", atts)

      case YWeatherHandler.YFCAST =>
        if (forecastCount < 2) {
          val forecast = new WeatherForecast()
          forecast setDate getAttributeValue("date", atts)
          forecast setDay getAttributeValue("day", atts)
          forecast setHigh getAttributeValue("high", atts).toInt
          forecast setLow getAttributeValue("low", atts).toInt
          val code = getAttributeValue("code", atts).toInt
          val cond = WeatherCondition.getWeatherCondition(code)
          forecast setCondition cond
          weatherRecord.getForecasts(forecastCount) = forecast
        }
        forecastCount += 1

      case _ =>
    }
  }

  @throws(classOf[SAXException])
  override def endElement(namespaceURI: String, localName: String, qName: String) {}

  override def characters(ch: Array[Char], start: Int, length: Int) {}

  private def getAttributeValue(attName: String, atts: Attributes): String = {
    var result: String = null
    var found = false
    for (i <- 0 until atts.getLength if !found) {
      val thisAtt = atts getLocalName i
      if (attName equals thisAtt) {
        result = atts getValue i
        found = true
      }
    }
    result
  }

  private def convertDirection(d: Int): String =
    if      (d >= 348.75 && d <  11.25) "N"
    else if (d >=  11.25 && d <  33.75) "NNE"
    else if (d >=  33.75 && d <  56.25) "NE"
    else if (d >=  56.25 && d <  78.75) "ENE"
    else if (d >=  78.75 && d < 101.25) "E"
    else if (d >= 101.25 && d < 123.75) "ESE"
    else if (d >= 123.75 && d < 146.25) "SE"
    else if (d >= 146.25 && d < 168.75) "SSE"
    else if (d >= 168.75 && d < 191.25) "S"
    else if (d >= 191.25 && d < 213.75) "SSW"
    else if (d >= 213.75 && d < 236.25) "SW"
    else if (d >= 236.25 && d < 258.75) "WSW"
    else if (d >= 258.75 && d < 281.25) "W"
    else if (d >= 281.25 && d < 303.75) "WNW"
    else if (d >= 303.75 && d < 326.25) "NW"
    else if (d >= 326.25 && d < 348.75) "NNW"
    else ""

  def getWeatherRecord: WeatherRecord = weatherRecord

  def setWeatherRecord(weatherRecord: WeatherRecord) {
    this.weatherRecord = weatherRecord
  }
}

object YWeatherHandler {
  // private final val CLASSTAG = classOf[YWeatherHandler].getSimpleName

  private final val YLOC   = "location"
  private final val YWIND  = "wind"
  private final val YATMO  = "atmosphere"
  private final val YASTRO = "astronomy"
  private final val YCOND  = "condition"
  private final val YFCAST = "forecast"
}
