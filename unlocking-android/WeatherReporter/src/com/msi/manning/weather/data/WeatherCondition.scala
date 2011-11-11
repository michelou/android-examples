package com.msi.manning.weather.data

/**
 * Enum to represent weather condtion states coming from Yahoo Weather API.
 * 
 * Image: http://l.yimg.com/us.yimg.com/i/us/we/52/[CODE].gif"
 * 
 * @author charliecollins
 * 
 */
object WeatherCondition extends Enumeration {
  val BLOWING_SNOW     = WeatherCond("Blowing Snow", 15)
  val BLUSTERY         = WeatherCond("Blustery", 23)
  val CLEAR_NIGHT      = WeatherCond("Clear (night)", 31)
  val CLOUDY           = WeatherCond("Cloudy", 26)
  val COLD             = WeatherCond("Cold", 25)
  val DRIZZLE          = WeatherCond("Drizzle", 9)
  val DUST             = WeatherCond("Dust", 19)
  val FAIR_DAY         = WeatherCond("Fair (day)", 34)
  val FAIR_NIGHT       = WeatherCond("Fair (night)", 33)
  val FOGGY            = WeatherCond("Foggy", 20)
  val FREEZING_DRIZZLE = WeatherCond("Freezing Drizzle", 8)
  val FREEZING_RAIN    = WeatherCond("Freezing Rain", 10)
  val HAIL             = WeatherCond("Hail", 17)
  val HAZE             = WeatherCond("Haze", 21)
  val HEAVY_SNOW       = WeatherCond("Heavy Snow", 41)
  val HEAVY_SNOW_WINDY = WeatherCond("Heavy Snow (windy)", 43)
  val HOT              = WeatherCond("Hot", 36)
  val HURRICANE        = WeatherCond("Hurricane", 2)
  val ISOLATED_THUNDERSHOWERS = WeatherCond("Isolated Thundershowers", 47)
  val ISOLATED_THUNDERSTORMS  = WeatherCond("Isolated Thunderstorms", 37)
  val LIGHT_SNOW_SHOWERS      = WeatherCond("Light Snow Showers", 14)
  val MIXED_RAIN_AND_HAIL     = WeatherCond("Mixed Rain and Hail", 35)
  val MIXED_RAIN_AND_SLEET    = WeatherCond("Mixed Rain and Sleet", 6)
  val MIXED_RAIN_AND_SNOW     = WeatherCond("Mixed Rain and Snow", 5)
  val MIXED_SNOW_AND_SLEET    = WeatherCond("Mixed Snow and Sleet", 7)
  val MOSTLY_CLOUDY_DAY       = WeatherCond("Mostly Cloudy (day)", 28)
  val MOSTLY_CLOUDY_NIGHT     = WeatherCond("Mostly Cloudy (night)", 27)
  val NOT_AVAILABLE           = WeatherCond("Not Available", 3200)
  val PARTLY_CLOUDY           = WeatherCond("Partly Cloudy", 44)
  val PARTLY_CLOUDY_DAY       = WeatherCond("Partly Cloudy (day)", 30)
  val PARTLY_CLOUDY_NIGHT     = WeatherCond("Partly Cloudy (night)", 29)
  val SCATTERED_SHOWERS       = WeatherCond("Scattered Showers", 40)
  val SCATTERED_SNOW_SHOWERS  = WeatherCond("Scattered Snow Showers", 42)
  val SCATTERED_THUNDERSTORMS_HEAVY = WeatherCond("Scattered Thunderstorms (heavy)", 39)
  val SCATTERED_THUNDERSTORMS_LIGHT = WeatherCond("Scattered Thunderstorms (light)", 38)
  val SEVERE_THUNDERSTORMS          = WeatherCond("Severe Thunderstorms", 3)
  val SNOW_SHOWERS   = WeatherCond("Snow Showers", 46)
  val SNOW_FLURRIES  = WeatherCond("Snow Flurries", 13)
  val SHOWERS_HEAVY  = WeatherCond("Heavy Showers", 12)
  val SHOWERS_LIGHT  = WeatherCond("Light Showers", 11)
  val SLEET          = WeatherCond("Sleet", 18)
  val SMOKY          = WeatherCond("Smoky", 22)
  val SNOW           = WeatherCond("Snow", 16)
  val SUNNY          = WeatherCond("Sunny", 32)
  val THUNDERSTORMS  = WeatherCond("Thunderstorms", 4)
  val THUNDERSHOWERS = WeatherCond("Thundershowers", 45)
  val TORNADO        = WeatherCond("Tornado", 0)
  val TROPICAL_STORM = WeatherCond("Tropical Storm", 1)
  val WINDY          = WeatherCond("Windy", 24)

  type WeatherCondition = WeatherCond

  private final val IMAGE_PREFIX = "http://l.yimg.com/us.yimg.com/i/us/we/52/"

  case class WeatherCond(var display: String, wid: Int) extends Val(display) {
    var netImageLink = IMAGE_PREFIX + wid + ".gif"
  }

  def getWeatherCondition(code: Int): WeatherCondition = code match {
    case  0 => TORNADO
    case  1 => TROPICAL_STORM
    case  2 => HURRICANE
    case  3 => SEVERE_THUNDERSTORMS
    case  4 => THUNDERSTORMS
    case  5 => MIXED_RAIN_AND_SNOW
    case  6 => MIXED_RAIN_AND_SLEET
    case  7 => MIXED_SNOW_AND_SLEET
    case  8 => FREEZING_DRIZZLE
    case  9 => DRIZZLE
    case 10 => FREEZING_RAIN
    case 11 => SHOWERS_LIGHT
    case 12 => SHOWERS_HEAVY
    case 13 => SNOW_FLURRIES
    case 14 => LIGHT_SNOW_SHOWERS
    case 15 => BLOWING_SNOW
    case 16 => SNOW
    case 17 => HAIL
    case 18 => SLEET
    case 19 => DUST
    case 20 => FOGGY
    case 21 => HAZE
    case 22 => SMOKY
    case 23 => BLUSTERY
    case 24 => WINDY
    case 25 => COLD
    case 26 => CLOUDY
    case 27 => MOSTLY_CLOUDY_NIGHT
    case 28 => MOSTLY_CLOUDY_DAY
    case 29 => PARTLY_CLOUDY_NIGHT
    case 30 => PARTLY_CLOUDY_DAY
    case 31 => CLEAR_NIGHT
    case 32 => SUNNY
    case 33 => FAIR_NIGHT
    case 34 => FAIR_DAY
    case 35 => MIXED_RAIN_AND_HAIL
    case 36 => HOT
    case 37 => ISOLATED_THUNDERSTORMS
    case 38 => SCATTERED_THUNDERSTORMS_LIGHT
    case 39 => SCATTERED_THUNDERSTORMS_HEAVY
    case 40 => SCATTERED_SHOWERS
    case 41 => HEAVY_SNOW
    case 42 => SCATTERED_SNOW_SHOWERS
    case 43 => HEAVY_SNOW_WINDY
    case 44 => PARTLY_CLOUDY
    case 45 => THUNDERSHOWERS
    case 46 => SNOW_SHOWERS
    case 47 => ISOLATED_THUNDERSHOWERS
    case _  => NOT_AVAILABLE
  }
}
