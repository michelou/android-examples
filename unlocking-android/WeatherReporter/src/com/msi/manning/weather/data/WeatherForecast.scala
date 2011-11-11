package com.msi.manning.weather.data

import WeatherCondition._

class WeatherForecast {
  private var date: String = _
  private var day: String = _
  private var high: Int = _
  private var low: Int = _
  private var condition: WeatherCondition = _

  def this(date: String, day: String,
           high: Int, low: Int, condition: WeatherCondition) {
    this()
    this.date = date
    this.day = day
    this.high = high
    this.low = low
    this.condition = condition
  }

  def getDate: String = date
  def setDate(date: String) { this.date = date }

  def getDay: String = day
  def setDay(day: String) { this.day = day }

  def getHigh: Int = high
  def setHigh(high: Int) { this.high = high }

  def getLow: Int = low
  def setLow(low: Int) { this.low = low }

  def getCondition: WeatherCondition = condition
  def setCondition(condition: WeatherCondition) { this.condition = condition }

  override def toString: String = {
    val sb = new StringBuilder()
    sb append "WeatherForecast:"
    sb append " date-" append date
    sb append " day-" append day
    sb append " high-" append high
    sb append " low-" append low
    if (condition != null) {
      sb append " condition-" append condition.display
    }
    sb.toString
  }

}
