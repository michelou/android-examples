package com.manning.aip.portfolio

import android.os.{Parcel, Parcelable}

/**
 * A data structure class representing a stock. This implements the
 * [[http://developer.android.com/reference/android/os/Parcelable.html Parcelable]]
 * interface so that it can be use for IPC between a background `Service`
 * and an `Activity`.
 * 
 * @author Michael Galpin
 *
 */
class Stock(private var symbol: String,
            private var pricePaid: Double,
            private var quantity: Int,
            private var id: Int) extends Parcelable {

  // auxiliary constructors
  def this(symbol: String, pricePaid: Double, quantity: Int) =
    this(symbol, pricePaid, quantity, 0)

  def this(old: Stock, id: Int) = {
    this(old.getSymbol, old.getPricePaid, old.getQuantity, id)
    this.maxPrice = old.getMaxPrice
    this.minPrice = old.getMinPrice
    this.name = old.getName
    this.currentPrice = old.getCurrentPrice
  }

  private def this(parcel: Parcel) {
    this(null, 0D, 0, 0)
    this.readFromParcel(parcel)
  }

  // user defined
  private var maxPrice: Double = _
  private var minPrice: Double = _

  // dynamic retrieved
  private var name = ""
  private var currentPrice = 0D

  // getters and setters
  def getName: String = name
  def setName(name: String) { this.name = name }

  def getCurrentPrice: Double = currentPrice
  def setCurrentPrice(currentPrice: Double) { this.currentPrice = currentPrice }

  def getSymbol: String = symbol

  def getMaxPrice: Double = maxPrice
  def setMaxPrice(maxPrice: Double) { this.maxPrice = maxPrice }

  def getMinPrice: Double = minPrice
  def setMinPrice(minPrice: Double) { this.minPrice = minPrice }

  def getPricePaid: Double = pricePaid

  def getQuantity: Int = quantity

  def getId: Int = id

  override def toString: String = {
    val sb = new StringBuilder
    if (name != null)
      sb append name append ' '
    sb append '(' append symbol.toUpperCase append ')' append " $" append currentPrice
    sb.toString
  }

  def describeContents: Int = 0

  override def writeToParcel(parcel: Parcel, flags: Int) {
    parcel writeString symbol
    parcel writeDouble maxPrice
    parcel writeDouble minPrice
    parcel writeDouble pricePaid
    parcel writeInt quantity
    parcel writeDouble currentPrice
    parcel writeString name
  }

  /**
   * Method for creating a `Stock` from a `Parcelable`.
   * This is not required by the `Parcelable` interface, you can instead
   * defer this to `Parcelable.Creator`'s `createFromParcel` method.
   * 
   * @param parcel The `Parcelable` being used to create
   *               a `Stock` object, presumably this is
   *               a `Stock` object that has been 
   *               serialized using the {@link #writeToParcel(Parcel, int) writeToParcel}
   *               method
   */
  def readFromParcel(parcel: Parcel) {
    symbol = parcel.readString()
    maxPrice = parcel.readDouble()
    minPrice = parcel.readDouble()
    pricePaid = parcel.readDouble()
    quantity = parcel.readInt()
    currentPrice = parcel.readDouble()
    name = parcel.readString()
  }
}

object Stock {
  /**
   * Any `Parcelable` needs a static field called `CREATOR` that
   * acts as a factory class for the `Parcelable`.
   */
  final val CREATOR = new Parcelable.Creator[Stock] {
    def createFromParcel(source: Parcel): Stock = new Stock(source)
    def newArray(size: Int): Array[Stock] = new Array[Stock](size)
  }
}

