package com.manning.aip.dealdroid.xml

import android.util.{Log, Xml}

import com.manning.aip.dealdroid.Constants
import com.manning.aip.dealdroid.model.{Item, Section}

import org.xmlpull.v1.XmlPullParser

import scala.collection.mutable

class DailyDealsXmlPullFeedParser extends DailyDealsFeedParser {
  val DailyDeals = DailyDealsXmlPullFeedParser // local alias

  def parse(): List[Section] = {
    ///Log.d(Constants.LOG_TAG, "parse invoked")
    val sections = new mutable.ListBuffer[Section]
    val parser = Xml.newPullParser
    try {
      var currentSection: Section = null
      var currentItem: Item = null

      // auto-detect the encoding from the stream
      parser.setInput(DailyDeals.getInputStream, null)
      var eventType = parser.getEventType

      while (eventType != XmlPullParser.END_DOCUMENT) {
        var name: String = null
        eventType match {
          case XmlPullParser.START_DOCUMENT =>
            ///Log.d(Constants.LOG_TAG, " start document")
          case XmlPullParser.START_TAG =>
            name = parser.getName
            ///Log.d(Constants.LOG_TAG, "  start tag: " + name)

            // establish sections
            if (name equalsIgnoreCase DailyDeals.EBAY_DAILY_DEALS) {
              ///Log.d(Constants.LOG_TAG, "   created section: Daily Deals")
              currentSection = Section("Daily Deals")
            } else if (name equalsIgnoreCase DailyDeals.SECTION_TITLE) {
              ///Log.d(Constants.LOG_TAG, "   created more details section: " + title)
              currentSection = Section(parser.nextText())
            } else if ((name equalsIgnoreCase DailyDeals.ITEM) &&
                       (currentSection != null)) {
              currentItem = new Item
            }

            // when MoreDeals starts, DailyDeals are over, more are nested (which is odd)
            if (name equalsIgnoreCase DailyDeals.MORE_DEALS) {
              ///Log.d(Constants.LOG_TAG, "   adding Daily Deals section to sections list")
              sections += currentSection
              currentSection = null
            }

            // establish items
            if ((currentSection != null) && (currentItem != null)) {
              if (name equalsIgnoreCase DailyDeals.ITEM_ID) {
                currentItem setItemId DailyDeals.nextLong(parser, DailyDeals.ITEM_ID)
              } else if (name equalsIgnoreCase DailyDeals.END_TIME) {
                currentItem setEndTime DailyDeals.nextLong(parser, DailyDeals.END_TIME)
              } else if (name equalsIgnoreCase DailyDeals.PICTURE_URL) {
                currentItem setPicUrl parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.SMALL_PICTURE_URL) {
                currentItem setSmallPicUrl parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.PICTURE_175_URL) {
                currentItem setPic175Url parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.TITLE) {
                currentItem setTitle parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.DESCRIPTION) {
                currentItem setDesc parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.DEAL_URL) {
                currentItem setDealUrl parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.CONVERTED_CURRENT_PRICE) {
                currentItem setConvertedCurrentPrice parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.PRIMARY_CATEGORY_NAME) {
                currentItem setPrimaryCategoryName parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.LOCATION) {
                currentItem setLocation parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.QUANTITY) {
                currentItem setQuantity DailyDeals.nextInt(parser, DailyDeals.QUANTITY)
              } else if (name equalsIgnoreCase DailyDeals.QUANTITY_SOLD) {
                currentItem setQuantitySold DailyDeals.nextInt(parser, DailyDeals.QUANTITY_SOLD)
              } else if (name equalsIgnoreCase DailyDeals.MSRP) {
                currentItem setMsrp parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.SAVINGS_RATE) {
                currentItem setSavingsRate parser.nextText()
              } else if (name equalsIgnoreCase DailyDeals.HOT) {
                currentItem setHot parser.nextText().toBoolean
              }
            }
          case XmlPullParser.END_TAG =>
            name = parser.getName
            ///Log.d(Constants.LOG_TAG, "  end tag: " + name);
            if (name != null) {
              if ((name equalsIgnoreCase DailyDeals.MORE_DEALS_SECTION) &&
                  (currentSection != null)) {
                ///Log.d(Constants.LOG_TAG, "   adding section to sections list: " + currentSection.title)
                sections += currentSection
                currentSection = null
              } else if ((name equalsIgnoreCase DailyDeals.ITEM) &&
                        (currentItem != null) && (currentSection != null)) {
                ///Log.d(Constants.LOG_TAG, "   adding item " + currentItem.title + " to current section")
                currentSection += currentItem
                currentItem = null
              }
            }
          case _ =>
        }
        eventType = parser.next()
      }
    } catch {
       case e: Exception =>
         Log.e(Constants.LOG_TAG, "Exception parsing XML", e)
         throw new RuntimeException(e)
    }
    sections.toList
  }
}

object DailyDealsXmlPullFeedParser {
  final val FEED_URL = "http://deals.ebay.com/feeds/xml"

  // names of the XML tags
  final val EBAY_DAILY_DEALS = "EbayDailyDeals"
  final val MORE_DEALS = "MoreDeals"
  final val MORE_DEALS_SECTION = "MoreDealsSection"
  final val SECTION_TITLE = "SectionTitle"

  final val ITEM = "Item"
  final val ITEM_ID = "ItemId"
  final val END_TIME = "EndTime"
  final val PICTURE_URL = "PictureURL"
  final val SMALL_PICTURE_URL = "SmallPictureURL"
  final val PICTURE_175_URL = "Picture175URL"
  final val TITLE = "Title"
  final val DESCRIPTION = "Description"
  final val DEAL_URL = "DealURL"
  final val CONVERTED_CURRENT_PRICE = "ConvertedCurrentPrice"
  final val PRIMARY_CATEGORY_NAME = "PrimaryCategoryName"
  final val LOCATION = "Location"
  final val QUANTITY = "Quantity"
  final val QUANTITY_SOLD = "QuantitySold"
  final val MSRP = "MSRP"
  final val SAVINGS_RATE = "SavingsRate"
  final val HOT = "Hot"

  import java.io.{IOException, InputStream}
  import java.net.{MalformedURLException, URL}

  private def getInputStream: InputStream =
    try {
      val feedUrl = new URL(FEED_URL)
      Log.d(Constants.LOG_TAG, "DailyDealsXmlPullFeedParser instantiated for URL:" + feedUrl)

      val conn = feedUrl.openConnection()
      conn setConnectTimeout 3000
      conn setReadTimeout 5000
      conn.getInputStream
    } catch {
      case e: MalformedURLException =>
        throw new RuntimeException(e)
      case e: IOException =>
        throw new RuntimeException(e)
    }

  private def nextLong(parser: XmlPullParser, s: String) =
    try {
      parser.nextText().toLong
    } catch {
      case e: NumberFormatException =>
        Log.e(Constants.LOG_TAG, "Error parsing "+s, e)
    }

  private def nextInt(parser: XmlPullParser, s: String) =
    try {
      parser.nextText().toInt
    } catch {
      case e: NumberFormatException =>
        Log.e(Constants.LOG_TAG, "Error parsing "+s, e)
    }
}
