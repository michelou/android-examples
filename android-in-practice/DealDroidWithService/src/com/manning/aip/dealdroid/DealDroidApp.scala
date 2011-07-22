package com.manning.aip.dealdroid

import android.app.Application
import android.content.{Context,SharedPreferences}
import android.content.SharedPreferences.Editor
import android.graphics.{Bitmap, BitmapFactory}
import android.net.{ConnectivityManager, NetworkInfo}
import android.net.NetworkInfo.State
import android.preference.PreferenceManager
import android.util.Log

import model.{Item, Section}
import xml.{DailyDealsFeedParser, DailyDealsXmlPullFeedParser}

import java.util.{ArrayList => JArrayList, List => JList}

import scala.collection.mutable

class DealDroidApp extends Application {

  private var cMgr: ConnectivityManager = _
  private var parser: DailyDealsFeedParser = _ 
  private var sectionList: JList[Section] = _
  private var imageCache: mutable.Map[Long, Bitmap] = _
  private var currentItem: Item = _
  private var prefs: SharedPreferences = _
   
  //
  // getters/setters
  //
  def getParser: DailyDealsFeedParser = this.parser

  def getSectionList: JList[Section] = this.sectionList

  def getImage(id: Long): Option[Bitmap] = imageCache get id
  def addImage(id: Long, bitmap: Bitmap) { imageCache += (id -> bitmap) }

  def getCurrentItem: Item = this.currentItem
  def setCurrentItem(currentItem: Item) { this.currentItem = currentItem }

  def getPrefs: SharedPreferences = this.prefs

  //
  // lifecycle
  //
  override def onCreate() {
    super.onCreate()
    this.cMgr = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    this.parser = new DailyDealsXmlPullFeedParser
    this.imageCache = new mutable.HashMap[Long, Bitmap]
    this.sectionList = new JArrayList[Section](6)
    this.prefs = PreferenceManager.getDefaultSharedPreferences(this)
  }

  override def onTerminate() {
    // not guaranteed to be called
    super.onTerminate()
  }

  //
  // previous deal state (used by service)
  //
  def getPreviousDealIds: JList[Long] = {
    val previousDealIds = new JArrayList[Long]
    previousDealIds add prefs.getLong(Constants.DEAL1, 0)
    previousDealIds add prefs.getLong(Constants.DEAL2, 0)
    previousDealIds add prefs.getLong(Constants.DEAL3, 0)
    previousDealIds add prefs.getLong(Constants.DEAL4, 0)
    previousDealIds add prefs.getLong(Constants.DEAL5, 0)
    previousDealIds
  }

  def setPreviousDealIds(previousDealIds: JList[Long]) {
    // should never get this error, but it's a good idea to fail fast in case
    if ((previousDealIds == null) || previousDealIds.isEmpty || previousDealIds.size > 5) {
      throw new IllegalArgumentException("Error, previousDealIds null, or empty, or more than 5")
    }
    val editor = prefs.edit
    editor.putLong(Constants.DEAL1, previousDealIds.get(0))
    editor.putLong(Constants.DEAL2, previousDealIds.get(1))
    editor.putLong(Constants.DEAL3, previousDealIds.get(2))
    editor.putLong(Constants.DEAL4, previousDealIds.get(3))
    // we added support for 5th deal later, some users may only have 4 in file
    // (one day last year eBay had 5 daily deals at once, it's normally 4)
    if (previousDealIds.size == 4) {
      previousDealIds add 0L
    }
    editor.putLong(Constants.DEAL5, previousDealIds.get(4))
    editor.commit()
  }

  def parseItemsIntoDealIds(items: JList[Item]): JList[Long] = {
    val idList = new JArrayList[Long]
    if ((items != null) && !items.isEmpty) {
      import collection.JavaConversions._
      for (item <- items) {
        idList add item.getItemId
      }
    }
    idList
  }

  def connectionPresent: Boolean = {
    val netInfo = cMgr.getActiveNetworkInfo
    if (netInfo != null && netInfo.getState != null)
      netInfo.getState equals State.CONNECTED
    else
      false
  }
}

object DealDroidApp {
  import java.io.IOException
  import java.net.{MalformedURLException, URL}

  //
  // helper methods (used by more than one other activity, so placed here, 
  // could be util class too)
  //
  def retrieveBitmap(urlString: String): Bitmap = {
    Log.d(Constants.LOG_TAG, "making HTTP trip for image:" + urlString)
    var bitmap: Bitmap = null
    try {
      val url = new URL(urlString)
      // NOTE, be careful about just doing "url.openStream()"  
      // it's a shortcut for openConnection().getInputStream() and doesn't set timeouts
      // (the defaults are "infinite" so it will wait forever if endpoint server is down)
      // do it properly with a few more lines of code ...         
      val conn = url.openConnection()
      conn setConnectTimeout 3000
      conn setReadTimeout 5000
      bitmap = BitmapFactory.decodeStream(conn.getInputStream)
    } catch {
      case e: MalformedURLException =>
        Log.e(Constants.LOG_TAG, "Exception loading image, malformed URL", e)
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "Exception loading image, IO error", e)
    } 
    bitmap
  }
}