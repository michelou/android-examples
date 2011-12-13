package com.msi.manning.weather.data

/**
 * Bean to represent a specific weather record as taken from Yahoo Weather API.
 * 
 * Units are: temperature="F" distance="mi" pressure="in" speed="mph"
 * 
 * @author charliecollins
 * 
 */
class WeatherRecord(private var overrideSevere: Boolean) {
  import WeatherCondition._, WeatherRecord._  // companion object

  private var severe: Boolean = _
  private var date: String = _
  private var temp: Int = _
  private var city: String = _
  private var region: String = _
  private var country: String = _
  private var windDirection: String = _
  private var windSpeed: Int = _
  private var windChill: String = _
  private var humidity: Int = _
  private var visibility: Int = _
  private var pressure: Double = _
  private var pressureState: String = _
  private var sunrise: String = _
  private var sunset: String = _
  private var link: String = _
  private var condition: WeatherCondition = _
  private var forecasts = new Array[WeatherForecast](2)

  def this() = this(false)

  def isOverrideSevere: Boolean = overrideSevere

  def setOverrideSevere(overrideSevere: Boolean) {
    this.overrideSevere = overrideSevere
  }

  def isSevere: Boolean = {
    // if any FORECAST is one of "severeConditions" then it's severe
    // (or if it's overridden for dev)
    if (overrideSevere) 
      return true
    if (forecasts != null)
      for (i <- 0 until forecasts.length if !severe) {
        val cond = forecasts(i).getCondition
        if (cond != null && severeConditions.contains(cond)) {
          severe = true
        }
      }
    severe
  }

  def getDate: String = date
  def setDate(date: String) { this.date = date }

  def getTemp: Int = temp
  def setTemp(temp: Int) { this.temp = temp }

  def getCity: String = city
  def setCity(city: String) { this.city = city }

  def getRegion: String = this.region
  def setRegion(region: String) { this.region = region }

  def getCountry: String = this.country
  def setCountry(country: String) { this.country = country }

  def getWindDirection: String = this.windDirection
  def setWindDirection(windDirection: String) {
    this.windDirection = windDirection
  }

  def getWindSpeed: Int = windSpeed
  def setWindSpeed(windSpeed: Int) { this.windSpeed = windSpeed }

  def getWindChill: String = windChill
  def setWindChill(windChill: String) { this.windChill = windChill }

  def getHumidity: Int = humidity
  def setHumidity(humidity: Int) { this.humidity = humidity }

  def getVisibility: Int = visibility
  def setVisibility(visibility: Int) { this.visibility = visibility }

  def getPressure: Double = pressure
  def setPressure(pressure: Double) { this.pressure = pressure }

  def getPressureState: String = pressureState
  def setPressureState(pressureState: String) {
    this.pressureState = pressureState
  }

  def getSunrise: String = sunrise
  def setSunrise(sunrise: String) { this.sunrise = sunrise }

  def getSunset: String = sunset
  def setSunset(sunset: String) { this.sunset = sunset }

  def getLink: String = link
  def setLink(link: String) { this.link = link }

  def getCondition: WeatherCondition = condition
  def setCondition(condition: WeatherCondition) {
    this.condition = condition
  }

  def getForecasts: Array[WeatherForecast] = forecasts
  def setForecasts(forecasts: Array[WeatherForecast]) {
    this.forecasts = forecasts
  }

  override def toString: String = {
    val sb = new StringBuffer()
    sb append "WeatherRecord:"
    sb append " city-" append city
    sb append " region-" append region
    sb append " country-" append country
    sb append " date-" append (if (date != null) date else "null")
    sb append " temp-" append temp
    sb append " windDirection-" append windDirection
    sb append " windSpeed-" append windSpeed
    sb append " windChill-" append windChill
    sb append " humidity-" append humidity
    sb append " visibility-" append visibility
    sb append " pressure-" append pressure
    sb append " pressureState-" append pressureState
    sb append " sunrise-" append sunrise
    sb append " sunset-" append sunset
    sb append " link-" append link
    sb append " condition-" append (if (condition != null) condition.display else "null")
    if (forecasts != null)
      for (forecast <- forecasts) {
        if (forecast != null)
          sb append " " append forecast.toString
        else
          sb append " forecast-null"
      }
    else
      sb append " forecasts-null"
    sb.toString
  }
}

object WeatherRecord {
  import WeatherCondition._

  final val PRESSURE_RISING  = "Rising"  // 1
  final val PRESSURE_STEADY  = "Steady"  // 0
  final val PRESSURE_FALLING = "Falling" // 2

  private val severeConditions = List(
    BLOWING_SNOW, FREEZING_RAIN, FREEZING_DRIZZLE, HAIL,
    HEAVY_SNOW, HEAVY_SNOW_WINDY, HURRICANE, SEVERE_THUNDERSTORMS,
    TORNADO, TROPICAL_STORM
  )
}
