package com.manning.aip.mymoviesdatabase

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.MotionEvent

import java.util.{Timer, TimerTask}

class SplashScreen extends Activity {
  import SplashScreen._

  private var app: MyMoviesApp = _
  private var prefs: SharedPreferences = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.splash_screen)

    app = getApplication.asInstanceOf[MyMoviesApp]
    prefs = app.getPrefs
  }

  override protected def onStart() {
    super.onStart()
    checkPrefsAndSplash()
  }

  private def checkPrefsAndSplash() {

    val splashSeenOnce = prefs.getBoolean("splashseenonce", false)
    if (!splashSeenOnce) {
      val editor = prefs.edit()
      editor.putBoolean("splashseenonce", true)
      editor.commit()
    }

    val showSplash = prefs.getBoolean("showsplash", false)
    if (!showSplash && splashSeenOnce) {
      proceed()
    } else {
      new Timer().schedule(new TimerTask() {
        override def run() {
          proceed()
        }
      }, SplashScreen.SPLASH_TIMEOUT)
    }
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    if (event.getAction == MotionEvent.ACTION_DOWN) {
      proceed()
    }
    super.onTouchEvent(event)
  }

  private def proceed() {
    if (this.isFinishing) {
      return
    }
    startActivity(new Intent(SplashScreen.this, classOf[MyMovies]))
    finish()
  }
}

object SplashScreen {
  final val SPLASH_TIMEOUT = 2000
}
