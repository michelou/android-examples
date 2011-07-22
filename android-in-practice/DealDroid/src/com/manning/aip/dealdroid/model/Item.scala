package com.manning.aip.dealdroid.model

final class Item {

  private var itemId: Long = _
  private var endTime: Long = _
  private var picUrl: String = _
  private var smallPicUrl: String = _
  private var pic175Url: String = _
  private var title: String = _
  private var desc: String = _
  private var dealUrl: String = _
  private var convertedCurrentPrice: String = _
  private var primaryCategoryName: String = _
  private var location: String = _
  private var quantity: Int = _
  private var quantitySold: Int = _
  private var msrp: String = _
  private var savingsRate: String = _
  private var hot: Boolean = _

  def getItemId: Long = this.itemId
  def setItemId(itemId: Long) { this.itemId = itemId }

  def getEndTime: Long = this.endTime
  def setEndTime(endTime: Long) { this.endTime = endTime }

  def getPicUrl: String = this.picUrl
  def setPicUrl(picUrl: String) { this.picUrl = picUrl }

  def getSmallPicUrl: String = this.smallPicUrl
  def setSmallPicUrl(smallPicUrl: String) { this.smallPicUrl = smallPicUrl }

  def getPic175Url: String = this.pic175Url
  def setPic175Url(pic175Url: String) { this.pic175Url = pic175Url }

  def getTitle: String = this.title
  def setTitle(title: String) { this.title = title }

  def getDesc: String = this.desc
  def setDesc(desc: String) { this.desc = desc }

  def getDealUrl: String = this.dealUrl
  def setDealUrl(dealUrl: String) { this.dealUrl = dealUrl }

  def getConvertedCurrentPrice: String = this.convertedCurrentPrice
  def setConvertedCurrentPrice(convertedCurrentPrice: String) {
    this.convertedCurrentPrice = convertedCurrentPrice
  }

  def getPrimaryCategoryName: String = this.primaryCategoryName
  def setPrimaryCategoryName(primaryCategoryName: String) {
    this.primaryCategoryName = primaryCategoryName
  }

  def getLocation: String = this.location
  def setLocation(location: String) { this.location = location }

  def getQuantity: Int = this.quantity
  def setQuantity(quantity: Int) { this.quantity = quantity }

  def getQuantitySold: Int = this.quantitySold
  def setQuantitySold(quantitySold: Int) { this.quantitySold = quantitySold }

  def getMsrp: String = this.msrp
  def setMsrp(msrp: String) { this.msrp = msrp }

  def getSavingsRate: String = this.savingsRate
  def setSavingsRate(savingsRate: String) { this.savingsRate = savingsRate }

  def isHot: Boolean = this.hot
  def setHot(hot: Boolean) { this.hot = hot }

  override def toString: String =
    "Item [convertedCurrentPrice=" + this.convertedCurrentPrice +
    ", dealUrl=" + this.dealUrl + ", desc=" + this.desc +
    ", endTime=" + this.endTime + ", hot=" + this.hot +
    ", itemId=" + this.itemId + ", location=" + this.location +
    ", msrp=" + this.msrp + ", pic175Url=" + this.pic175Url +
    ", picUrl=" + this.picUrl + ", primaryCategoryName=" + this.primaryCategoryName +
    ", quantity=" + this.quantity + ", quantitySold=" + this.quantitySold +
    ", savingsRate=" + this.savingsRate + ", smallPicUrl=" + this.smallPicUrl +
    ", title=" + this.title + "]"

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (this.convertedCurrentPrice == null) 0 else this.convertedCurrentPrice.hashCode)
    result = prime * result + (if (this.dealUrl == null) 0 else this.dealUrl.hashCode)
    result = prime * result + (if (this.desc == null) 0 else this.desc.hashCode)
    // end time and hot not part of hashCode
    result = prime * result + (this.itemId ^ (this.itemId >>> 32)).toInt
    result = prime * result + (if (this.location == null) 0 else this.location.hashCode)
    result = prime * result + (if (this.msrp == null) 0 else this.msrp.hashCode)
    result = prime * result + (if (this.pic175Url == null) 0 else this.pic175Url.hashCode)
    result = prime * result + (if (this.picUrl == null) 0 else this.picUrl.hashCode)
    result = prime * result + (if (this.primaryCategoryName == null) 0 else this.primaryCategoryName.hashCode)
    // quantity sold and quantity not part of hashCode
    result = prime * result + (if (this.savingsRate == null) 0 else this.savingsRate.hashCode)
    result = prime * result + (if (this.smallPicUrl == null) 0 else this.smallPicUrl.hashCode)
    result = prime * result + (if (this.title == null) 0 else this.title.hashCode)
    result
  }

  override def equals(other: Any): Boolean = other match {
    case that: Item =>
      // end time, hot, quanity sold and quanity not part of equals
      (this.itemId == that.itemId) &&
      (this.convertedCurrentPrice == that.convertedCurrentPrice) &&
      (this.dealUrl == that.dealUrl) &&
      (this.desc == that.desc) &&
      (this.location == that.location) &&
      (this.msrp == that.msrp) &&
      (this.pic175Url == that.pic175Url) &&
      (this.smallPicUrl == that.smallPicUrl) &&
      (this.picUrl == that.picUrl) &&
      (this.primaryCategoryName == that.primaryCategoryName) &&
      (this.savingsRate == that.savingsRate) &&
      (this.title == that.title)
    case _ =>
      false
  }
}

object Item {

  // favor "copy constructor/getInstance" over clone, clone is tricky and error prone
  // (better yet use immutable objects, but sort of overkill for this example)
  def getInstance(item: Item): Item = {
    val copy = new Item
    copy.convertedCurrentPrice = item.convertedCurrentPrice
    copy.dealUrl = item.dealUrl
    copy.desc = item.desc
    copy.endTime = item.endTime
    copy.hot = item.hot
    copy.itemId = item.itemId
    copy.location = item.location
    copy.msrp = item.msrp
    copy.picUrl = item.picUrl
    copy.primaryCategoryName = item.primaryCategoryName
    copy.quantity = item.quantity
    copy.quantitySold = item.quantitySold
    copy.savingsRate = item.savingsRate
    copy.smallPicUrl = item.smallPicUrl
    copy.title = item.title
    copy.pic175Url = item.pic175Url
    copy
  }
}