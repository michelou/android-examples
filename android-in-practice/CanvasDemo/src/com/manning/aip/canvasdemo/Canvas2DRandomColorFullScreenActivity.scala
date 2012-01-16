package com.manning.aip.canvasdemo

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.{View, Window, WindowManager}

import scala.util.Random

class Canvas2DRandomColorFullScreenActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    requestWindowFeature(Window.FEATURE_NO_TITLE)
    getWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

    setContentView(new CanvasView(this))
  }

  class CanvasView(context: Context) extends View(context) {
    private val random = new Random()

    override protected def onDraw(canvas: Canvas) {
      canvas.drawRGB(random nextInt 256, random nextInt 256 , random nextInt 256)
    }
  }
}
