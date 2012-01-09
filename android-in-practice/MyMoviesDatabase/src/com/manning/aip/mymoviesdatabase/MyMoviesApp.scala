package com.manning.aip.mymoviesdatabase

import android.app.Application
import android.content.{Context, SharedPreferences}
import android.net.{ConnectivityManager, NetworkInfo}
import android.net.NetworkInfo.State
import android.preference.PreferenceManager

import data.{DataManager, DataManagerImpl}
import util.ImageCache

class MyMoviesApp extends Application {
  private var cMgr: ConnectivityManager = _
  private var dataManager: DataManager = _
  private var imageCache: ImageCache = _
  private var prefs: SharedPreferences = _

  //
  // getters/setters
  //   
  def getPrefs: SharedPreferences = prefs
  def getDataManager: DataManager = dataManager
  def getImageCache: ImageCache = imageCache

  //
  // lifecycle
  //
  override def onCreate() {
    super.onCreate()
    cMgr = this.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    prefs = PreferenceManager.getDefaultSharedPreferences(this)
    dataManager = new DataManagerImpl(this)
    imageCache = new ImageCache()
  }

  override def onTerminate() {
    // not guaranteed to be called
    super.onTerminate()
  }

  //
  // util/helpers for app
  //
  def connectionPresent: Boolean = {
    val netInfo = cMgr.getActiveNetworkInfo
    if ((netInfo != null) && (netInfo.getState != null))
      netInfo.getState equals State.CONNECTED
    else
      false
  }
}
