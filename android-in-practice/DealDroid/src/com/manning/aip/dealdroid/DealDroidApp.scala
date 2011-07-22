package com.manning.aip.dealdroid

import android.app.Application
import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.net.{ConnectivityManager, NetworkInfo}
import android.net.NetworkInfo.State
import android.util.Log

import java.util.{ArrayList => JArrayList, List => JList}

import model.{Item, Section}
import xml.{DailyDealsFeedParser, DailyDealsXmlPullFeedParser}

import scala.collection.mutable

class DealDroidApp extends Application {

  private var cMgr: ConnectivityManager = _
  private var parser: DailyDealsFeedParser = _
  private var sectionList: JList[Section] = _
  private var imageCache: mutable.Map[Long, Bitmap] = _
  private var currentItem: Item = _

  //
  // getters/setters
  //
  def getParser: DailyDealsFeedParser = this.parser

  def getSectionList: JList[Section] = this.sectionList

  def getImage(id: Long): Option[Bitmap] = imageCache get id
  def addImage(id: Long, bitmap: Bitmap) { imageCache += (id -> bitmap) }

  def getCurrentItem: Item = this.currentItem
  def setCurrentItem(currentItem: Item) { this.currentItem = currentItem }

  //
  // lifecycle
  //
  override def onCreate() {
    super.onCreate()
    this.cMgr = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    this.parser = new DailyDealsXmlPullFeedParser
    this.sectionList = new JArrayList[Section](6)
    this.imageCache = new mutable.HashMap[Long, Bitmap]
  }

  override def onTerminate() {
    // not guaranteed to be called
    super.onTerminate()
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