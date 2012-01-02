package com.manning.aip

import java.util.{Timer, TimerTask}

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent

class SplashScreen extends Activity {
  import SplashScreen._

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.splash_screen)

    new Timer().schedule(new TimerTask() {
      override def run() { proceed() }
    }, SPLASH_TIMEOUT)
  }

  override def onTouchEvent(event: MotionEvent):  Boolean = {
    if (event.getAction == MotionEvent.ACTION_DOWN)
      proceed()
    super.onTouchEvent(event)
  }

  private def proceed() {
    if (this.isFinishing) return
    startActivity(new Intent(SplashScreen.this, classOf[MyMovies]))
    finish()
  }
}

object SplashScreen {
  final val SPLASH_TIMEOUT = 2000
}
